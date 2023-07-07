package com.atspring.springpro.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atspring.common.utils.R;
import com.atspring.springpro.ware.Feign.MemberFeignService;
import com.atspring.springpro.ware.vo.FareVo;
import com.atspring.springpro.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atspring.common.utils.PageUtils;
import com.atspring.common.utils.Query;

import com.atspring.springpro.ware.dao.WareInfoDao;
import com.atspring.springpro.ware.entity.WareInfoEntity;
import com.atspring.springpro.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wareInfoEntityQueryWrapper.eq("id",key).or()
                    .like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {

        FareVo fareVo = new FareVo();
        R r =memberFeignService.addrInfo(addrId);
        MemberAddressVo data=r.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>(){});
        if(data!=null){
            String phone = data.getPhone();
            String substring =phone.substring(phone.length()-1);
            BigDecimal bigDecimal = new BigDecimal(substring);
            fareVo.setAddress(data);
            fareVo.setFare(bigDecimal);
            return fareVo;
        }
        return null;
    }

}