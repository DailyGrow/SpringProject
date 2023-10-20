package com.atspring.springpro.order.dao;

import com.atspring.springpro.order.entity.OrderReturnReasonEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退货原因
 * 
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:39:13
 */
@Mapper
public interface OrderReturnReasonDao extends BaseMapper<OrderReturnReasonEntity> {
	
}
