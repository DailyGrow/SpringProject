package com.atspring.springpro.product;


import com.atspring.springpro.product.entity.BrandEntity;
import com.atspring.springpro.product.service.BrandService;
import com.atspring.springpro.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/*

1.引入oss-starter
2.配置key, endpoint相关信息
3.使用OSSClient进行相关操作
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringproProductApplication.class)
public class SpringproProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Autowired
    RedissonClient redissonClient;

    @Test
    public void redisson(){
        System.out.println(redissonClient);
    }

    @Test
    public void teststringRedisTemplate(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        //保存
        ops.set("hello","world"+ UUID.randomUUID().toString());

        //查询
        String hello = ops.get("hello");
        System.out.println("保存的数据："+hello);
    }


    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完成路径, {}", Arrays.asList(catelogPath));
    }

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
