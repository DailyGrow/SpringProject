package com.atspring.springpro.product.service;

import com.atspring.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.springpro.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-05 18:30:05
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

