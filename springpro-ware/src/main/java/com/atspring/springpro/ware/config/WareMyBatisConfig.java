package com.atspring.springpro.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement //开启事务
@MapperScan("com.atspring.springpro.ware.dao") //开启mybatis 扫描
@Configuration
public class WareMyBatisConfig {
    //引入分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //设置请求的页面大于页后操作,true调回到首页，false继续请求，默认false
        //paginationInterceptor.setOverflow(true);
        //设置最大单页限制数量
        //paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
