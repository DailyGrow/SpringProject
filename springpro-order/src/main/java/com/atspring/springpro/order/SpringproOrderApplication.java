package com.atspring.springpro.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
使用RabbitMQ
1.引入amqp场景：RabbitAutoConfiguration就会自动生效
2.给容器中自动配置了 RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
所有的属性都是在"spring.rabbitmq"  public class RabbitProperties绑定
3.给配置文件中配置spring。rabbitmq信息
4.开启rabbitmq相关功能@EnableRabbit
 */

@EnableRabbit
@SpringBootApplication
public class SpringproOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringproOrderApplication.class, args);
    }

}
