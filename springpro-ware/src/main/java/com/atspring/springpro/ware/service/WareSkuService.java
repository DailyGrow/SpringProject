package com.atspring.springpro.ware.service;

import com.atspring.springpro.ware.vo.SkuHasStockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:48:25
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);
}

