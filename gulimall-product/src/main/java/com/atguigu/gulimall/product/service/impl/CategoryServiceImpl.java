package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有的分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        return entities.stream().filter(e -> e.getCatLevel() == 1).map(e -> {
            e.setChildren(getChildrens(e, entities));
            return e;
        }).sorted((e1, e2) -> Optional.ofNullable(e1.getSort()).orElse(0) - Optional.ofNullable(e2.getSort()).orElse(0))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        baseMapper.deleteBatchIds(asList);
    }


    private List<CategoryEntity> getChildrens(CategoryEntity categoryEntity, List<CategoryEntity> entities) {
        return entities.stream().filter(e -> categoryEntity.getCatId().equals(e.getParentCid())).map(e -> {
            e.setChildren(getChildrens(e, entities));
            return e;
        }).sorted((e1, e2) -> Optional.ofNullable(e1.getSort()).orElse(0) - Optional.ofNullable(e2.getSort()).orElse(0))
                .collect(Collectors.toList());
    }

}