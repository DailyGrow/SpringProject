package com.atspring.springpro.product.web;

import com.atspring.springpro.product.service.SkuInfoService;
import com.atspring.springpro.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;
    /*
    展示当前sku的详情
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {

        SkuItemVo vo = skuInfoService.item(skuId);
        model.addAttribute("item",vo);
        return "item";
    }
}
