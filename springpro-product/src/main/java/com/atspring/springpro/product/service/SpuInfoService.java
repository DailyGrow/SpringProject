package com.atspring.springpro.product.service;

import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.product.entity.SpuInfoDescEntity;
import com.atspring.springpro.product.vo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.springpro.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-05 18:30:05
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity infoEntity);


    PageUtils queryPageByCondition(Map<String, Object> params);

    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);

}

