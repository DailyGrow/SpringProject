package com.atspring.springpro.coupon.dao;

import com.atspring.springpro.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:09:19
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
