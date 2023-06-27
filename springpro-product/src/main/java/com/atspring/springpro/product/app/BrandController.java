package com.atspring.springpro.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atspring.common.validator.group.AddGroup;
import com.atspring.common.validator.group.UpdateGroup;
import com.atspring.common.validator.group.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atspring.springpro.product.entity.BrandEntity;
import com.atspring.springpro.product.service.BrandService;
import com.atspring.common.utils.PageUtils;
import com.atspring.common.utils.R;


/**
 * 品牌
 *
 * @author cbei
 * @email 858519155@gmail.com
 * @date 2023-05-09 16:16:39
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> brand = brandService.getBrandByIds(brandIds);

        return R.ok().put("brand", brand);
    }
    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand){

        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改status
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
