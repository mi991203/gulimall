package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.Map;

/**
 * 商品三级分类
 *
 * @author leifengyang
 * @email sh19991203@163.com
 * @date 2022-12-23 00:27:03
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
