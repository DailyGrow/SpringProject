package com.atspring.springpro.product.feign;


import com.atspring.common.to.SkuReductionTo;
import com.atspring.common.to.SpuBoundTo;
import com.atspring.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("springpro-coupon") //注解调用哪个远程服务
public interface CouponFeignService {

    /*
    1.CouponFeignService.saveSpuBounds(spuBoundTo)
        1)用@RequestBody注解将这个对象转为json
        2)spring cloud 回去注册中心找到springpro-coupon这个服务，然后给/coupon/spubounds/save发送请求。
        由于标了requestbody，会将上一步转的json对象放在请求体位置，发送请求
        3)对方服务收到请求，请求体里有json数据
        对方服务（@RequestBody SpuBoundsEntity spuBounds）将请求体里的json转为对象SpuBoundsEntity
     所以只要json数据模型是兼容的，双方服务无需使用同一个to
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo); //要跟远程的接口保证一致的签名


    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);

}
