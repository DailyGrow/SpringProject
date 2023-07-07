package com.atspring.springpro.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class SpringproFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {

            @Override
            public void apply(RequestTemplate template) {

                //因为浏览器发toTrade请求，来到service,service发远程调用请求，feign创建对象时调用拦截器，故拦截器与controller、service都是同一个线程

                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();//该方法本事是Threadlocal，拿到当前的请求
                if(attributes!=null){
                    HttpServletRequest request = attributes.getRequest();

                    if(request!=null){
                        //同步请求头数据，cookie
                        String cookie = request.getHeader("Cookie");
                        //给新请求同步了它的cookie
                        template.header("Cookie",cookie);
                    }
                }


            }
        };

    }
}
