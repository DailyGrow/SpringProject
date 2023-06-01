package com.atspring.springpro.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/*

    使用mybatis做逻辑删除
    1)配置全局的逻辑删除删除规则（可省略）
    2）低版本需要配置删除的组件Bean
    3）给Bean加上逻辑删除注解@TableLogic
*/

@EnableDiscoveryClient //开启服务注册发现功能
@MapperScan("com/atspring/springpro/product/dao")
@SpringBootApplication
public class SpringproProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringproProductApplication.class, args);
    }

}
