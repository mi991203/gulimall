package com.atguigu.gulimall.product.controller;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private SkuInfoService skuInfoService;

    @GetMapping("sku/{skuId}")
    public SkuItemVo skuItem(@PathVariable("skuId") Long skuId) throws ExecutionException, InterruptedException {
        return skuInfoService.item(skuId);
    }
}
