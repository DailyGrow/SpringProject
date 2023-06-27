package com.atspring.springpro.product.service;

import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.product.vo.SkuItemSaleAttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.springpro.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-05 18:30:05
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(Long skuId);

}

