package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.service.CacheService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CacheServiceImpl implements CacheService {
    @Resource
    private CacheServiceImpl self;

    @Override
    public String queryCache(String msg) {
        return self.getMsg(msg);
    }

    @Cacheable(value = {"cache1", "cache2"}, key = "'queryCache'")
    public String getMsg(String msg) {
        return msg;
    }
}
