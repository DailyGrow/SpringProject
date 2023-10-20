package com.atspring.springpro.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atspring.common.exception.NoStockException;
import com.atspring.common.to.mq.OrderTo;
import com.atspring.common.to.mq.StockDetailTo;
import com.atspring.common.to.mq.StockLockedTo;
import com.atspring.common.utils.R;
import com.atspring.springpro.ware.Feign.OrderFeignService;
import com.atspring.springpro.ware.Feign.ProductFeignService;
import com.atspring.springpro.ware.entity.WareOrderTaskDetailEntity;
import com.atspring.springpro.ware.entity.WareOrderTaskEntity;
import com.atspring.springpro.ware.service.WareOrderTaskDetailService;
import com.atspring.springpro.ware.service.WareOrderTaskService;
import com.atspring.springpro.ware.vo.OrderItemVo;
import com.atspring.springpro.ware.vo.OrderVo;
import com.atspring.springpro.ware.vo.SkuHasStockVo;
import com.atspring.springpro.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atspring.common.utils.PageUtils;
import com.atspring.common.utils.Query;

import com.atspring.springpro.ware.dao.WareSkuDao;
import com.atspring.springpro.ware.entity.WareSkuEntity;
import com.atspring.springpro.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;


    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        wareSkuDao.unlockStock(skuId, wareId, num);
        //更新库存工作单状态·
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2); //变为已解锁
        orderTaskDetailService.updateById(entity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {


        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        //1.判断如果还没有这个库存记录，做新增操作

        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {

            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //远程查询sku的名字，如果失败，整个事务无需回滚(通过自己catch异常)
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }

            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            //查询sku当前的总库存量
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());

        return collect;

    }

    /**
     * 为订单锁定库存
     *
     * @param vo
     * @return 库存解锁的场景
     * 1）下订单成功，订单过期没有支付被系统自动取消、被用户手动取消，都要解锁库存
     * 2）下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前锁定的库存就要自动解锁（如果用seata太慢）
     */
    @Transactional(rollbackFor = NoStockException.class) //标注事务，默认在抛出异常时都会回滚
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {


        /**
         * 保存库存工作单的详情.追溯
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);


        //1.找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;

        }).collect(Collectors.toList());

        //2.锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                //没有任何仓库有这个库存
                throw new NoStockException(skuId);
            }

            //1.如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给mq
            //2.锁定失败，前面保存的工作单信息就回滚了。发送出去的消息，即使要解锁记录，由于数据库查不到
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    //TODO:告诉mq锁定成功

                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(entity);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());

                    //只发id不行，防止回滚以后找不到工作单详情数据
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    lockedTo.setDetail(stockDetailTo);

                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);

                    break;
                } else {
                    //当前仓库锁失败，重试下一个仓库
                }

            }
            if (skuStocked == false) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }

        }

        return true;

    }

    /**
     * MQ鼻综合增消息可靠性：防止消息重复，将业务设计成幂等
     * @param to
     */
    @Override //库存解锁逻辑
    public void unlockStock(StockLockedTo to) {



        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        //解锁
        //1.查询数据库关于这个订单的锁定库存信息
        //有：证明库存锁定成功，然后还需看订单情况1）没有这个订单，解锁2）有这个订单，若订单状态是已取消，则解锁库存，若没取消，则不能解锁
        //没有：本身库存锁定失败，，库存已经回滚，这种情况无需解锁
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            //需要解锁
            Long id = to.getId(); //库存工作单的id
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();//根据订单号查询订单的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                //订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });

                if (data == null || data.getStatus() == 4) {
                    //订单不存在或已经被取消，解锁库存

                    if(byId.getLockStatus() == 1){
                        //只有当前库存工作单详情状态1 已锁定但未解锁时才可以解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }

                    //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            } else {
                //消息拒绝以后重新放到队列里面，让别人继续解锁
                //channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
                throw new RuntimeException("远程调用失败");
            }
        } else {
            //没有改详情单，无需解锁
            //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

    }

    //防止订单服务卡顿，导致订单状态消息改不了，库存消息优先到期，查到订单状态为新建状态，什么都没有做。导致卡顿的订单无法解锁库存
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查一下最新库存的状态，防止重复解锁库存
        WareOrderTaskEntity task = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        //按照工作单找到所有没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> entities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id)
                .eq("lock_status", 1));

        for(WareOrderTaskDetailEntity entity:entities){
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}