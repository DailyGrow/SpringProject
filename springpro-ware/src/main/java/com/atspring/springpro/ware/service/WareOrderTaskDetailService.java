package com.atspring.springpro.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atspring.common.utils.PageUtils;
import com.atspring.springpro.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-10 20:48:25
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

