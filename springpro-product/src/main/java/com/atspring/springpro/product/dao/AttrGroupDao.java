package com.atspring.springpro.product.dao;

import com.atspring.springpro.product.entity.AttrGroupEntity;
import com.atspring.springpro.product.vo.SkuItemVo;
import com.atspring.springpro.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-05 18:30:05
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);

}
