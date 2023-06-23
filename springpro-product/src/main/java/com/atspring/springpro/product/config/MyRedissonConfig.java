package com.atspring.springpro.product.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    /*
    所有对redisson的使用都是通过redissonClient对象
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException{
        //1创建配置(程序化配置)
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");

        //2.根据config创建出RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
