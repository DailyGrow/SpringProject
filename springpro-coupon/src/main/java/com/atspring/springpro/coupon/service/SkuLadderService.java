package com.atspring.springpro.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.coupon.entity.SkuLadderEntity;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:09:19
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

