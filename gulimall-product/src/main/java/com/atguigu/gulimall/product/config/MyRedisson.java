package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedisson {
    @Bean
    public RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.137.11:6379");
        return Redisson.create(config);
    }
}
