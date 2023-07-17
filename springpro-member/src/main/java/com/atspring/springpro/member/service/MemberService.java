package com.atspring.springpro.member.service;

import com.atspring.springpro.member.exception.PhoneExistException;
import com.atspring.springpro.member.exception.UsernameExistException;
import com.atspring.springpro.member.vo.MemberLoginVo;
import com.atspring.springpro.member.vo.MemberRegistVo;
import com.atspring.springpro.member.vo.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:20:21
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;

}

