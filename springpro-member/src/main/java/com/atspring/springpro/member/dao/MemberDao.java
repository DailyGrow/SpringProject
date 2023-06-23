package com.atspring.springpro.member.dao;

import com.atspring.springpro.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:20:21
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
