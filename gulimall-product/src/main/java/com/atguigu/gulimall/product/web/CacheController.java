package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.CacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class CacheController {
    @Resource
    private CacheService cacheService;

    @GetMapping("/cache-test/{msg}")
    public String cache(@PathVariable String msg) {
        return cacheService.queryCache(msg);
    }
}
