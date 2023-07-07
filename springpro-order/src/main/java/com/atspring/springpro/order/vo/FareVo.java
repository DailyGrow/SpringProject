package com.atspring.springpro.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    private MemberAddressVo adress;
    private BigDecimal fare;
}
