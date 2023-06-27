package com.atspring.springpro.cart.Vo;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class UserInfoTo {

    private Long userId;
    private String userKey;

    private  boolean tempUser = false;
}
