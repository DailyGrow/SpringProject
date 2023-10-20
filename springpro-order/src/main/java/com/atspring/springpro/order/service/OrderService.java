package com.atspring.springpro.order.service;

import com.atspring.springpro.order.vo.OrderConfirmVo;
import com.atspring.springpro.order.vo.OrderSubmitVo;
import com.atspring.springpro.order.vo.SubmitOrderResponseVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:39:13
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回需要用的数据
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 下单
     * @param vo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

}

