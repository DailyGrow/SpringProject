package com.atspring.springpro.member.fegin;

import com.atspring.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;


@FeignClient("springpro-coupon")
public interface CouponFeginService {

    @RequestMapping("/coupon/coupon/member/list")
    public R membercoupons();

}
