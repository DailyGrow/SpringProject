package com.atspring.springpro.order.feign;

import com.atspring.springpro.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("springpro-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memeberId}/addresses")
    List<MemberAddressVo> getAddress(@PathVariable("memeberId") Long memberId);
}
