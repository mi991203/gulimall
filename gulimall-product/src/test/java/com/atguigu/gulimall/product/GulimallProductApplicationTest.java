package com.atguigu.gulimall.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    public void contextLoads() {

    }
}
