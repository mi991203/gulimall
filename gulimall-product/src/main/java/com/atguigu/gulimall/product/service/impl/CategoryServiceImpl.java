package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Resource
    private RedissonClient redissonClient;

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
    @CacheEvict(value = "category", key = "'getLevel1Categorys'")
    @Transactional(rollbackFor = Exception.class)
    public void updateCascade(CategoryEntity category) {
        this.baseMapper.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(
                new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        final RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("catagory-lock");
        final RLock rLock = readWriteLock.readLock();
        Map<String, List<Catelog2Vo>> dataFromDb = null;
        try {
            rLock.lock();
            dataFromDb = getCatalogDataFromDB();
        } finally {
            rLock.unlock();
        }
        return dataFromDb;
    }

    private Map<String, List<Catelog2Vo>> getCatalogDataFromDB() {
        // 得到了读锁之后，需要再去Redis确认一次缓存是否存在
        final String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        // 可能当缓存失效的时候，多个请求进来，避免多次查库。添加分布式锁
        final RLock lock = redissonClient.getLock("catagory-lock-query-db");
        try {
            lock.lock();
            final String catalogJson2 = stringRedisTemplate.opsForValue().get("catalogJson");
            if (!StringUtils.isEmpty(catalogJson2)) {
                return JSON.parseObject(catalogJson2, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
            }
            log.info("缓存中没有key:[{}]相关的缓存，将查询数据库", "catalogJson");
            {
                //将数据库的多次查询变为一次
                List<CategoryEntity> selectList = this.baseMapper.selectList(null);

                //1、查出所有分类
                //1、1）查出所有一级分类
                List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

                //封装数据
                Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    //1、每一个的一级分类,查到这个一级分类的二级分类
                    List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

                    //2、封装上面的结果
                    List<Catelog2Vo> catelog2Vos = null;
                    if (categoryEntities != null) {
                        catelog2Vos = categoryEntities.stream().map(l2 -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                            //1、找当前二级分类的三级分类封装成vo
                            List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                            if (level3Catelog != null) {
                                List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                                    //2、封装成指定格式
                                    Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                                    return category3Vo;
                                }).collect(Collectors.toList());
                                catelog2Vo.setCatalog3List(category3Vos);
                            }

                            return catelog2Vo;
                        }).collect(Collectors.toList());
                    }
                    return catelog2Vos;
                }));
                // 给缓存设置3分钟的超时时间
                stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(parentCid), 3, TimeUnit.MINUTES);
                return parentCid;
            }
        } finally {
            lock.unlock();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        // return this.baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
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