package com.atspring.springpro.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.atspring.springpro.member.fegin")
@EnableDiscoveryClient //开启服务注册与发现功能
@SpringBootApplication
public class SpringproMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringproMemberApplication.class, args);
    }

}
