package com.atspring.springpro.order.vo;

import com.atspring.springpro.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;
    private Integer code; //0 成功
}
