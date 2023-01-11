package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.exception.PhoneException;
import com.atguigu.gulimall.member.exception.UsernameException;
import com.atguigu.gulimall.member.vo.MemberUserLoginVo;
import com.atguigu.gulimall.member.vo.MemberUserRegisterVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author ShaoHong
 * @email sh1101315853@163.com
 * @date 2022-12-23 14:18:32
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);


    /**
     * 用户登录
     * @param vo
     * @return
     */
    MemberEntity login(MemberUserLoginVo vo);

    /**
     * 用户注册
     * @param vo
     */
    void register(MemberUserRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneException;

    void checkUserNameUnique(String userName) throws UsernameException;
}

