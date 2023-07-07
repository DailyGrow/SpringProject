package com.atspring.springpro.order;

import com.atspring.springpro.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringproOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageText(){

        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setName("hh");
        //发送消息，如果发送的消息是个对象吗，我们会使用序列化机制，将对象写出去，对象需实现Serializable

        //发送对象的消息，可以是json
        String msg = "hello world";
        //correlatiuonData:代表消息的唯一id
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",msg,new CorrelationData(UUID.randomUUID().toString()));
    }

    /*
    queues:声明需要监听的所有队列
    Message:原生消息详细信息：头+体
    T<发送消息的类型> OrderReturnReasonEntity content
    Channel channel:当前传输数据的通道

    场景:
    1)分布式下，订单服务启动多个：同一个消息，只能又一个客户端收到
    2）只有一个消息处理完，方法运行结束，我们才能接受到下一个消息
     */
    @RabbitListener(queues ={"hello-java-queue"})
    public void receiveMessage(Message message, OrderReturnReasonEntity content, Channel channel){

    }
    /*
    1.如何创建Exchange、Queue、Binding
        1)\使用AmqpAdmin进行创建
    2.如何收发消息
     */
    @Test
    public void createExchange(){
   //持久化指一开机一关机还有
        DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
    }

    @Test
    public void createQueue(){
        //排他队列指只能被声明的连接使用，其他人都连不上。实际开发中使用非排他的。大家都能连上队列，不过可能只有一个能够接受队列的消息
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
    }

    @Test
    public void createBinding(){

        //将exchange指定的交换机和destination目的地进行绑定，使用routingkey作为指定的路由键
        Binding binding = new Binding("hello-java-queue",Binding.DestinationType.QUEUE,"hello-java-exchange","heelo.java",null);

        amqpAdmin.declareBinding(binding);
    }

    @Test
    public void contextLoads() {
    }

}
