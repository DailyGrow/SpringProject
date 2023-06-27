package com.atspring.springpro.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.coupon.entity.SeckillSessionEntity;

import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:09:19
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

