package com.atspring.springpro.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/*
使用RabbitMQ
1.引入amqp场景：RabbitAutoConfiguration就会自动生效
2.给容器中自动配置了 RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
所有的属性都是在"spring.rabbitmq"  public class RabbitProperties绑定
3.给配置文件中配置spring。rabbitmq信息
4.开启rabbitmq相关功能@EnableRabbit
5.监听消息@RabbitListener可以标注在类和方法上，表示监听哪些队列；RabbitHandler可以标在方法上,用于重载区分不同类型的消息

本地事务失效问题
事务使用代理对象来控制。同一个对象内事务方法互调默认失效，原因绕过了代理对象
解决方案:使用代理对象来调用事务方法
1）引入spring-boot-starter-aop，使用其中的aspectj
2)开启aspectj动态代理 @EnableAspectJAutoProxy,而不是jdk默认代理，好处是即使没有接口也可以创建动态代理
exposeProxy = true 对外暴露代理对象
3）本类互调用调用对象 AopContext.currentProxy()拿到当前代理对象

Seata控制分布式事务
1)每一个微服务先创建undo_log表
2）安装seata-server TC事务协调器

 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients
@EnableRabbit
@SpringBootApplication
public class SpringproOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringproOrderApplication.class, args);
    }

}
