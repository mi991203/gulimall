package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallThirdPartyApplicationTest {
    @Resource
    private OSSClient ossClient;


    @Test
    public void context() {
        try {
            ossClient.putObject("gulimall-shao-1203", "application.yaml", this
                    .getClass().getClassLoader().getResourceAsStream("application.yaml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
