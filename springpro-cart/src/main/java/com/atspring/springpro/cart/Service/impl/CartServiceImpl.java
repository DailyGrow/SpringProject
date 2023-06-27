package com.atspring.springpro.cart.Service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atspring.common.utils.R;
import com.atspring.springpro.cart.Service.CartService;
import com.atspring.springpro.cart.Vo.Cart;
import com.atspring.springpro.cart.Vo.CartItem;
import com.atspring.springpro.cart.Vo.SkuInfoVo;
import com.atspring.springpro.cart.Vo.UserInfoTo;
import com.atspring.springpro.cart.feign.ProductFeignService;
import com.atspring.springpro.cart.interceptor.CartInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX = "springpro:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();



        String res = (String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(res)){
            //2.添加新商品到购物车

            CartItem cartItem = new CartItem();

            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(()->{
                //1.远程查询当前要添加的商品的信息
                R skuinfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuinfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });


                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            },executor);


            //远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask,getSkuSaleAttrValues).get();
            //向redis中保存信息
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);

            return cartItem;
        }else{
            //购物车有此商品，修改数量即可
            CartItem cartItem = JSON.parseObject(res,CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }



    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String str = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()!=null){
            //登录状态
            String cartkey = CART_PREFIX+ userInfoTo.getUserId();

            //如果临时购物车的数据还没有合并，需要合并购物车
            String tempCartkey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            if(tempCartItems!=null){
                //临时购物车有数据，需要合并
                for(CartItem item : tempCartItems){
                    addToCart(item.getSkuId(), item.getCount());
                }

                //加入后，清除临时购物车数据
                clearCart(tempCartkey);
            }

            //获取登录后的购物车的数据[包干合并过来的临时购物车的数据，和登录后的购物车的数据]
            List<CartItem> cartItems = getCartItems(cartkey);
            cart.setItems(cartItems);
        }else{
            //未登录状态
            String cartkey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
           List<CartItem> cartItems = getCartItems(cartkey);
           cart.setItems(cartItems);
        }

        return cart;
    }


    /*
    获取到我们要操作的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get(); //只要在同一次请求里面都可以通过threadlocal得到用户信息

        String cartkey = "";
        if(userInfoTo.getUserId()!=null){
            //springpro:cart:1
            cartkey = CART_PREFIX+userInfoTo.getUserId();
        }else{
            cartkey = CART_PREFIX + userInfoTo.getUserKey(); //临时购物车
        }

        //将商品添加进购物车
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartkey);

        return operations;
    }

    private List<CartItem> getCartItems(String cartkey){

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartkey);
        List<Object> values = hashOps.values();
        if(values!=null && values.size()>0){
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);

                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public void clearCart(String cartkey) {
       redisTemplate.delete(cartkey); //直接把临时购物车的key删掉

    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1?true:false);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        cartOps.delete(skuId.toString());
    }


}
