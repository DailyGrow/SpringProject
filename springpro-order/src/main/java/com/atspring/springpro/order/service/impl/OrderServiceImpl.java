package com.atspring.springpro.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atspring.common.exception.NoStockException;
import com.atspring.common.to.mq.OrderTo;
import com.atspring.common.utils.R;
import com.atspring.common.vo.MemberRespVo;
import com.atspring.springpro.order.constant.OrderConstant;
import com.atspring.springpro.order.entity.OrderItemEntity;
import com.atspring.springpro.order.enume.OrderStatusEnum;
import com.atspring.springpro.order.feign.CartFeignService;
import com.atspring.springpro.order.feign.MemberFeignService;
import com.atspring.springpro.order.feign.ProductFeignService;
import com.atspring.springpro.order.feign.WmsFeignService;
import com.atspring.springpro.order.interceptor.LoginUserInterceptor;
import com.atspring.springpro.order.service.OrderItemService;
import com.atspring.springpro.order.to.OrderCreateTo;
import com.atspring.springpro.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atspring.common.utils.PageUtils;
import com.atspring.common.utils.Query;

import com.atspring.springpro.order.dao.OrderDao;
import com.atspring.springpro.order.entity.OrderEntity;
import com.atspring.springpro.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.LoginUser.get();

        //获取之前的请求数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1.远程查询所有的收货地址列表
            RequestContextHolder.setRequestAttributes(requestAttributes); //在副线程共享主线程里threadlocal数据
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //2远程查询购物车所有选中的购物项
            //每一个线程都共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
            //feign在远程调用之前要构造请求，会创建一个新request,这个请求没有任何请头，调用很多拦截器 RequestInterceptor interceptor: requestInterceptors
            //所以feign远程调用会丢失请求头，解决方法是加上自己的feign远程调用的请求拦截器，apply request模板
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item->item.getSkuId()).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if(data!=null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        },executor);


        //3,查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        //4.其他数据自动计算

        //5.防重令牌
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture,cartFuture).get();

        return confirmVo;
    }

    //产生分布式事务的最大原因：网络问题+分布式机器
    @Transactional  //本地事务，在分布式系费中，只能控制住自己的回滚，控制不了其他服务的回滚
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.LoginUser.get();
        response.setCode(0);
        //去创建订单，验令牌，验价格，锁库存
        //1.验证令牌[令牌的对比和删除必须保证原子性]

        String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken=vo.getOrderToken();
        //原子验证令牌和删除令牌
        Long execute =redisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId()),orderToken);
        if(execute==0L){
            response.setCode(1);
            return response;
        }else{
            //验证成功
            OrderCreateTo order = createOrder();
            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if(Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01){
                //金额对比

                //保存订单
                saveOrder(order);

                //库存锁定,只要有异常就回滚订单数据
                //订单号，所有订单项（skuId, skuName, num）
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                //远程锁库存。可能出现状况：库存成功了，但是网络原因超时了，订单回滚，库存不回滚

                //为了保证高并发，可以发消息给库存服务，库存服务自己回滚，库存服务本身也可以使用自动解锁模式，参加消息队列即可
                R r =wmsFeignService.orderLockStock(lockVo);
                if(r.getCode()==0){
                    //锁库存成功
                    response.setOrder(order.getOrder());
                    //TODO:订单创建成功发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());

                    return response;
                }else{
                    //锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);

                }

            }else{
                response.setCode(2);
                return response;
            }
        }
//        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberRespVo.getId());
//        if(orderToken!=null && orderToken.equals(redisToken)){
//            //令牌验证通过
//        }else{
//            //不通过
//        }

    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {

        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn",orderSn));
        return order_sn;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());

        //关单
        if(orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            //因为消息队列的数据不是最新的，所有创建一个新的
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            //将关单信息给mq发出
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            //TODO:保证消息一定会发送出去，每一个消息都可以做好日志记录(给数据库保存每一个消息的详细信息)，
            //定期扫描数据库将失败的消息再发送一遍
            try{

                rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);
            }catch (Exception e){
                //将没发送成功的消息进行重视发送
            }


        }
    }

    /**
     * 保存订单数据
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);


    }

    private OrderCreateTo createOrder(){
        OrderCreateTo createTo = new OrderCreateTo();
        //1.生成订单号
        String orderSn = IdWorker.getTimeId();

        OrderEntity orderEntity=buildOrder(orderSn);

        //获取所有的订单项
       List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

       //验价
        computePrice(orderEntity, itemEntities);
        createTo.setOrder(orderEntity);
        createTo.setOrderItems(itemEntities);

        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        //订单的总额。叠加每一个订单项的总额信息
        for(OrderItemEntity entity: itemEntities){
            coupon = coupon.add(entity.getCouponAmount());
            integration = integration.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            total = total.add(entity.getRealAmount());
            gift = gift.add(new BigDecimal(entity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(entity.getGiftGrowth().toString()));

        }
        //1.订单价格相关的
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));

        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0); //0表示未删除

    }

    private OrderEntity buildOrder(String orderSn) {
        //创建订单号
        MemberRespVo respVo = LoginUserInterceptor.LoginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(respVo.getId());

        OrderSubmitVo submitVo = confirmVoThreadLocal.get();
        R fare =wmsFeignService.getFare(submitVo.getAddrId());
        //获取收货地址信息
        FareVo fareResp =fare.getData(new TypeReference<FareVo>(){});

        entity.setFreightAmount(fareResp.getFare());
        entity.setReceiverCity(fareResp.getAdress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAdress().getDetailAddress());
        entity.setReceiverName(fareResp.getAdress().getName());
        entity.setReceiverPhone(fareResp.getAdress().getPhone());
        entity.setReceiverPostCode(fareResp.getAdress().getPostCode());
        entity.setReceiverProvince(fareResp.getAdress().getProvince());
        entity.setReceiverRegion(fareResp.getAdress().getRegion());

        //设置订单的相关状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);

        return entity;
    }

    /**
     * 构建所有订单项数据
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后queing每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if(currentUserCartItems!=null && currentUserCartItems.size()>0){
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);

                itemEntity.setOrderSn(orderSn);

                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 构建指定订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {

        OrderItemEntity itemEntity = new OrderItemEntity();
        //商品的spu信息
        Long skuId = cartItem.getSkuId();
        R r =productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data =r.getData(new TypeReference<SpuInfoVo>(){});
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());

        //商品的sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr =StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(),";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());

        //积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        //订单价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal(("0")));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getPromotionAmount())
                        .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

}