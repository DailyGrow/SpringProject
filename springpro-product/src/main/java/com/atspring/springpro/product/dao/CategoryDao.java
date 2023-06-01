package com.atspring.springpro.product.dao;

import com.atspring.springpro.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-05 18:30:05
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
