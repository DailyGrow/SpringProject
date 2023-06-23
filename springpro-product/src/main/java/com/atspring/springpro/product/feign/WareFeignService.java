package com.atspring.springpro.product.feign;

import com.atspring.common.to.SkuHasStockVo;
import com.atspring.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("springpro-ware")
public interface WareFeignService {


    @PostMapping("/ware/waresku/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds);
}
