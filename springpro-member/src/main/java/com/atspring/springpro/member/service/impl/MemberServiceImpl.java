package com.atspring.springpro.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atspring.common.utils.HttpUtils;
import com.atspring.springpro.member.dao.MemberLevelDao;
import com.atspring.springpro.member.entity.MemberLevelEntity;
import com.atspring.springpro.member.exception.PhoneExistException;
import com.atspring.springpro.member.exception.UsernameExistException;
import com.atspring.springpro.member.vo.MemberLoginVo;
import com.atspring.springpro.member.vo.MemberRegistVo;
import com.atspring.springpro.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atspring.common.utils.PageUtils;
import com.atspring.common.utils.Query;

import com.atspring.springpro.member.dao.MemberDao;
import com.atspring.springpro.member.entity.MemberEntity;
import com.atspring.springpro.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());


        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());
        entity.setNickname(vo.getUserName());

        //密码要进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); //使用spring的密码编码器
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        memberDao.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile",phone));
        if(count > 0){
            throw new PhoneExistException();
        }

    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException{
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username",username));
        if(count>0){
            throw new UsernameExistException();
        }

    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {

        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username",loginacct).or().eq("mobile",loginacct));
        if(entity==null){
            //登录失败
            return null;
        }else{
            //获取到数据库的password
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            Boolean matches = passwordEncoder.matches(password, passwordDb);
            if(matches){
                return entity;
            }else{
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {

        try{
            Map<String, String> query = new HashMap<>();
            query.put("Authorization", "token " + socialUser.getAccess_token());
            HttpResponse response = HttpUtils.doGet("https://api.github.com","/user","get", query, new HashMap<String, String>());
            if(response.getStatusLine().getStatusCode()==200) {

                String json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSON.parseObject(json);
                String name = jsonObject.getString("login");
                String id = jsonObject.getString("id");

                MemberDao memberDao = this.baseMapper;
                MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", id));
                if (memberEntity != null) {
                    MemberEntity update = new MemberEntity();
                    update.setId(memberEntity.getId());
                    update.setAccessToken(socialUser.getAccess_token());
                    memberDao.updateById(update);

                    memberEntity.setAccessToken(socialUser.getAccess_token());
                    return memberEntity;

                } else {
                    MemberEntity regist = new MemberEntity();
                    regist.setNickname(name);
                    regist.setSocialUid(id);
                    regist.setAccessToken(socialUser.getAccess_token());
                    memberDao.insert(regist);
                    return regist;
                }
            }
        }catch(Exception e){

        }
        return new MemberEntity();

    }

}