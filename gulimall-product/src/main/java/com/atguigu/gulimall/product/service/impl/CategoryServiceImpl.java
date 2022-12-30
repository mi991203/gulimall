package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

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

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        final List<Long> resultList = new ArrayList<>();
        recursionCategoryTree(catelogId, resultList);
        return resultList.toArray(new Long[resultList.size()]);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCascade(CategoryEntity category) {
        this.baseMapper.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    private void recursionCategoryTree(Long catelogId, List<Long> resultList) {
        final CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        if (categoryEntity != null) {
            final Long parentCid = categoryEntity.getParentCid();
            if (parentCid != null) {
                recursionCategoryTree(parentCid, resultList);
            }
        }
        if (catelogId != 0) {
            resultList.add(catelogId);
        }
    }


    private List<CategoryEntity> getChildrens(CategoryEntity categoryEntity, List<CategoryEntity> entities) {
        return entities.stream().filter(e -> categoryEntity.getCatId().equals(e.getParentCid())).map(e -> {
            e.setChildren(getChildrens(e, entities));
            return e;
        }).sorted((e1, e2) -> Optional.ofNullable(e1.getSort()).orElse(0) - Optional.ofNullable(e2.getSort()).orElse(0))
                .collect(Collectors.toList());
    }

}