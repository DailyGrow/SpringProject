package com.atspring.springpro.cart.Vo;

import java.math.BigDecimal;
import java.util.List;

public class Cart {

    List<CartItem> items;

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    private Integer countNum; //商品数量

    private Integer countType; //商品类型数量

    public List<CartItem> getItems() {
        return items;
    }


    public Integer getCountNum() {
        int count = 0;
        if(items!=null && items.size()>0){
            for(CartItem item: items){
                countNum+=item.getCount();
            }
        }
        return count;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        int count = 0;
        if(items!=null && items.size()>0){
            for(CartItem item: items){
                countNum+=1;
            }
        }
        return count;
    }



    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if(items!=null && items.size()>0){
            for(CartItem item: items){
                if(item.getCheck()){
                    amount = amount.add(item.getTotalPrice());
                }

            }
        }
        BigDecimal substract = amount.subtract(getReduce());
        return substract;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }

    private BigDecimal totalAmount; //商品总价

    private BigDecimal reduce = new BigDecimal("0.00"); //减免价格


}
