package com.atspring.springpro.product.service;

import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.product.vo.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.springpro.product.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-05 18:30:05
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;

}

