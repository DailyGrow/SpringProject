package com.atspring.springpro.search.vo;

import lombok.Data;

import java.util.List;

/*
封装页面所有可能传递过来的查询条件,
 */
@Data
public class SearchParam {

    private String keyword; //页面传递过来的全文匹配关键字 keyword=

    private Long catalog3Id; //三级分类id catalog3Id=

    private String sort;//排序条件 sort=saleCount_asc/desc

    /*
    过滤条件:hasStock(是否有货), skuprice区间、brandId、catalog3Id、attrs
     */

    private Integer hasStock; //是否只显示有货

    private String skuPrice; //价格区间查询 1_500/_500

    private List<Long> brandId; //按照品牌进行查询，可以所选 brandId=3&brandId=2

    private List<String> attrs;//按照属性进行筛选

    private Integer pageNum = 1;//页码
}
