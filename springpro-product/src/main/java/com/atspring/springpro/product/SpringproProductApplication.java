package com.atspring.springpro.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/*

    1.使用mybatis做逻辑删除
    1)配置全局的逻辑删除删除规则（可省略）
    2）低版本需要配置删除的组件Bean
    3）给Bean加上逻辑删除注解@TableLogic

    2.JSR303
    1)给Bean添加校验注解: javax.validation.constraints,并定义自己的message提示
    2)开启校验功能@Valid, 效果：校验错误后会有默认的响应
    3）给校验的bean后紧跟一个BindingResult，就可以获得校验的结果
    4)分组校验(多场景的分组校验，比如新增、更新)
        1.给校验注解标注什么情况需要进行校验
        2@@Validated({Addgroup.class})
        3默认没有指定分组的校验注解@Notlank,在分组校验情况@Validated({AddGroup.class})下不生效
    5)自定义校验
        1编写一个自定义的校验注解、校验器ConstraintValidator，关联自定义的校验器和自定义的校验注解@Documented, @ConstraintBy
    3.统一的异常处理
    @ControllerAdvice
    1）编写异常处理类，使用@ControllerAdvice
    2)使用@ExceptionHandler标注方法可以处理的异常


    4.模板引擎
    1）thymeleaf-starter，生产模式下关闭缓存
    2）静态资源都放在static文件夹下就可以按照路径直接访问
    3）页面放在templates下，直接访问
    Springboot做了配置，访问项目的时候，默认会访问index

    5,整合redis
    1.引入data-redis-starter dependency
    2.简单配置redis的host信息
    3.使用springboot自动配置好的stringredisTemplate来操作redis

    6.整合redisson作为分布式锁等功能框架
    1)引入依赖
    2）配置redisson

    7.整合springcache简化缓存开发
    1)引入依赖
    2）配置
        1.cacheAutoConfiguration会导入rediscacheconfiguration，自动配好了缓存管理器Rediscachemanager
        2.配置使用redis作为缓存
        3.开启缓存功能@EnableCaching
        4.只需要使用注解就能玩成缓存操作
     3)原理：
     CacheAutoConfiguration -> RedisCacheConfiguration _>自动配置了RedisCacheManager->初始化所有的缓存->每个缓存决定使用什么配置
     ->
*/
//@EnableCaching //开启缓存功能 移到了mycacheconfig文件里
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.atspring.springpro.product.feign")
@EnableDiscoveryClient //开启服务注册发现功能
@MapperScan("com.atspring.springpro.product.dao")
@SpringBootApplication
public class SpringproProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringproProductApplication.class, args);
    }

}
