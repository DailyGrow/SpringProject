package com.atspring.springpro.product.service;

import com.atspring.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.springpro.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-05 18:30:05
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuImagesEntity> getImagesBySkuId(Long skuId);

}

