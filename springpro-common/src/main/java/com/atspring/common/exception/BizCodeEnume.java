package com.atspring.common.exception;

public enum BizCodeEnume {
    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    USER_EXIST_EXCEPTION(15001,"用户存在异常"),
    PHONE_EXIST_EXCEPTION(15002,"手机号存在异常"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003,"账号密码错误");


    private int code;
    private String msg;
    BizCodeEnume(int code, String msg){
        this.code =code;
        this.msg = msg;
    }

    public int getCode(){
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
