package com.atspring.springpro.member.dao;

import com.atspring.springpro.member.entity.MemberStatisticsInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员统计信息
 * 
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:20:20
 */
@Mapper
public interface MemberStatisticsInfoDao extends BaseMapper<MemberStatisticsInfoEntity> {
	
}
