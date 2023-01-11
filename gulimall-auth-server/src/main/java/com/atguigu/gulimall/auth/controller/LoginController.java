package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constance.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.MemberResponseVo;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegisterVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    @Resource
    private ThirdPartFeignService thirdPartFeignService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        // 60内每个手机号只能注册一次
        final String redisPhoneSms = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (redisPhoneSms != null) {
            // 验证是否在60秒限制以内
            final String timeMills = redisPhoneSms.split("-")[1];
            if (System.currentTimeMillis() - Long.parseLong(timeMills) < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        // 重新获取验证码，然后重刷redis
        final int codeNumber = ThreadLocalRandom.current().nextInt(1000, 9999);
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, codeNumber + "-" + System.currentTimeMillis(), 1, TimeUnit.HOURS);
        thirdPartFeignService.sendCode(phone, codeNumber + "");

        return R.ok();
    }

    @PostMapping(value = "/register")
    public String register(@Valid UserRegisterVo vos, BindingResult result,
                           RedirectAttributes attributes) {
        //如果有错误回到注册页面
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors",errors);

            //效验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //1、效验验证码
        String code = vos.getCode();

        //获取存入Redis里的验证码
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            //截取字符串
            if (code.equals(redisCode.split("-")[0])) {
                //删除验证码;令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX+vos.getPhone());
                //验证码通过，真正注册，调用远程服务进行注册
                R register = memberFeignService.register(vos);
                if (register.getCode() == 0) {
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    //失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", register.getData("msg",new TypeReference<String>(){}));
                    attributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }


            } else {
                //效验出错回到注册页面
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                attributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            //效验出错回到注册页面
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }


    @PostMapping(value = "/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session) {

        //远程登录
        R login = memberFeignService.login(vo);

        if (login.getCode() == 0) {
            MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>() {});
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
