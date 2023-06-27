package com.atspring.springpro.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.coupon.entity.SeckillSkuNoticeEntity;

import java.util.Map;

/**
 * 秒杀商品通知订阅
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:09:19
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

