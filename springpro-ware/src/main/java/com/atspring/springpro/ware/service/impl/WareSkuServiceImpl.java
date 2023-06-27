package com.atspring.springpro.ware.service.impl;

import com.atspring.common.utils.R;
import com.atspring.springpro.ware.Feign.ProductFeignService;
import com.atspring.springpro.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atspring.common.utils.PageUtils;
import com.atspring.common.utils.Query;

import com.atspring.springpro.ware.dao.WareSkuDao;
import com.atspring.springpro.ware.entity.WareSkuEntity;
import com.atspring.springpro.ware.service.WareSkuService;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {


        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {

        //1.判断如果还没有这个库存记录，做新增操作

        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id",skuId).eq("ware_id",wareId));
        if(entities == null || entities.size() ==0){

            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //远程查询sku的名字，如果失败，整个事务无需回滚(通过自己catch异常)
           try{
               R info = productFeignService.info(skuId);
               Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
               if(info.getCode() == 0){
                   skuEntity.setSkuName((String) data.get("skuName"));
               }
           }catch(Exception e){

           }

            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            //查询sku当前的总库存量
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count==null?false:count>0);
            return vo;
        }).collect(Collectors.toList());

        return collect;

    }

}