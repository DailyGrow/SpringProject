package com.atspring.springpro.product.vo;

import com.atspring.springpro.product.entity.SkuImagesEntity;
import com.atspring.springpro.product.entity.SkuInfoEntity;
import com.atspring.springpro.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    SkuInfoEntity info;

    boolean hasStock = true;

    List<SkuImagesEntity> images;

    List<SkuItemSaleAttrVo> saleAttr;

    SpuInfoDescEntity desp;

    List<SpuItemAttrGroupVo> groupAttrs;



}
