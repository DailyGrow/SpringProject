package com.atspring.springpro.ware.Feign;

import com.atspring.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("springpro-product")
public interface ProductFeignService {

    /*
    feign发送可以有两种方式
    1)让所有请求过网关，@feignClient("springpro-gateway"),给springpro所在的机器发请求,路径是/api/prodcut/skuinfo/info/{skuid}
    2)直接让后套指定服务处理 @FeignClient("springpro-producty"),路径是/product/skuinfop/info/{skuid}
     */
    @RequestMapping("/prodcut/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
