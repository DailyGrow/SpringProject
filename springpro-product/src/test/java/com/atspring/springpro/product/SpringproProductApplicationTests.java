package com.atspring.springpro.product;


import com.atspring.springpro.product.entity.BrandEntity;
import com.atspring.springpro.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/*

1.引入oss-starter
2.配置key, endpoint相关信息
3.使用OSSClient进行相关操作
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringproProductApplication.class)
public class SpringproProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
     public void contextLoads() {
        /*
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("华为");
        brandService.updateById(brandEntity);
        System.out.println("update successfully");*/
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id",1L));
        list.forEach((item)->{
            System.out.println(item);
        });
    }

}
