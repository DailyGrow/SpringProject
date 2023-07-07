package com.atspring.springpro.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.atspring.common.to.mq.OrderTo;
import com.atspring.common.to.mq.StockDetailTo;
import com.atspring.common.to.mq.StockLockedTo;
import com.atspring.common.utils.R;
import com.atspring.springpro.ware.entity.WareOrderTaskDetailEntity;
import com.atspring.springpro.ware.entity.WareOrderTaskEntity;
import com.atspring.springpro.ware.service.WareSkuService;
import com.atspring.springpro.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    /**
     * 库存自动解锁
     * 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前锁定的库存就要自动解锁
     * @param to
     * @param message
     *
     * 只要解锁库存的消息失败，一定要告诉服务解锁失败，
     */
    @RabbitHandler //监听
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {

        System.out.println("收到库存解锁单的消息");

        try{
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭准备解锁库存");
        try{
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
