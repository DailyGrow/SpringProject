package com.atspring.springpro.ware.service;

import com.atspring.springpro.ware.vo.FareVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.ware.entity.WareInfoEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:48:25
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据用户的收货地址计算运费
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);

}

