package com.atspring.springpro.order.config;

import com.atspring.springpro.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {


//    @RabbitListener(queues = "order.release.order.queue")
//    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
//        System.out.println("收到过期的订单");
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//    }
    /**
     * bean的作用：容器中的Binding、Queue、Exchange都会自动创建（前提是rabbitmq中没有这些）
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        //死信队列
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);

        Queue queue = new Queue("order.delay.queue",true,false,false, arguments);

        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue(){

        Queue queue = new Queue("order.release.order.queue",true,false,false);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);

    }

    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue",Binding.DestinationType.QUEUE,"order-event-exchange",
                "order.create.order",null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue",Binding.DestinationType.QUEUE,"order-event-exchange",
                "order.release.order",null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     */
    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue",Binding.DestinationType.QUEUE,"order-event-exchange",
                "order.release.other.#",null);
    }
}
