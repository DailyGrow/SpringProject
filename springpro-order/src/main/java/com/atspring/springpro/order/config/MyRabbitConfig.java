package com.atspring.springpro.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 使用JSON序列化机制，进行消息转换
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /*
    定制RabbitTemplate
    1.消息抵达服务代理确认：spring-rabbit.mq-publish-confirm=true,设置确认回调confirmCallback
    2.消息抵达队列确认:配置，returncallback

    3.消费端确认：默认是自动确认的，只要消息接收到，客户端自动确认，服务端就会移除这个消息。但若此时有消息还没处理完，服务宕机，会消失丢失
    改成消费者手动确认模式，只要没有明确告诉MQ，没有ack，消息就一直是unacked状态，即使consu宕机，消息也不会丢失，会重新变成ready
    签收时调用channel.basicAck（）; channel.basicNack();业务失败，拒签
     */

    @PostConstruct //表示config对象创建完成以后，执行这个方法
    public void initRabbitTemplate(){
        //设置确认回调，只要消息抵达broker，就会触发
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 当前消息的唯一关联数据（这个消息的唯一id）
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                /**
                 * 保证消息可靠性：处理消息丢失问题
                 * 1.做好消息确认机制（publisher，consumer【手动ack】）
                 * 2.每一个发送的消息都在数据库做好记录，定期扫描数据库将失败的消息再发送一遍
                 */
                System.out.println("confirm--correlationData["+correlationData+"]==>ack["+ack);
            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message 投递失败的消息详细信息
             * @param replycode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange 当时这个消息发给哪个交换机
             * @param routingkey 当时这个消息用哪个路由器
             */
            @Override
            public void returnedMessage(Message message, int replycode, String replyText, String exchange, String routingkey) {
                System.out.println("fail message");
            }
        });

    }
}
