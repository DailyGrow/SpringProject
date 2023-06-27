package com.atspring.springpro.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringproOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageText(){

        //发送消息，如果发送的消息是个对象吗，我们会使用序列化机制，将对象写出去，对象需实现Serializable

        //发送对象的消息，可以是json
        String msg = "hello world";
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",msg);
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
