package com.atspring.common.exception;

public class NoStockException extends  RuntimeException{

    private Long skuId;
    private String message;
    public NoStockException(Long skuId){
        super("商品id"+skuId+":没有足够的库存");
    }
    public NoStockException(String message){
        super(message);
    }



    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
