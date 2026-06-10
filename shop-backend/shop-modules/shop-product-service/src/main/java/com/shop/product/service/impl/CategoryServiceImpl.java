package com.shop.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.common.web.PageResult;
import com.shop.product.entity.Category;
import com.shop.product.mapper.CategoryMapper;
import com.shop.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品分类服务实现
 *
 * @author shop
 * @since 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CACHE_CATEGORY_TREE = "category:tree";

    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category create(Category category) {
        // 设置层级
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            Category parent = getEntityById(category.getParentId());
            category.setLevel(parent.getLevel() + 1);
            if (category.getLevel() > 3) {
                throw new BusinessException("分类层级最多为3级");
            }
        }

        baseMapper.insert(category);
        evictCategoryCache();
        log.info("创建分类成功: id={}, name={}", category.getId(), category.getName());
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category update(Long id, Category category) {
        Category entity = getEntityById(id);

        // 不允许修改层级
        category.setLevel(null);
        category.setParentId(null);

        if (category.getName() != null) {
            entity.setName(category.getName());
        }
        if (category.getIcon() != null) {
            entity.setIcon(category.getIcon());
        }
        if (category.getSort() != null) {
            entity.setSort(category.getSort());
        }
        if (category.getStatus() != null) {
            entity.setStatus(category.getStatus());
        }

        baseMapper.updateById(entity);
        evictCategoryCache();
        log.info("更新分类成功: id={}", id);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Category entity = getEntityById(id);

        // 检查是否有子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        wrapper.eq(Category::getDeleted, 0);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该分类下有子分类，不能删除");
        }

        baseMapper.deleteById(id);
        evictCategoryCache();
        log.info("删除分类成功: id={}", id);
    }

    @Override
    public Category getById(Long id) {
        return getEntityById(id);
    }

    @Override
    public PageResult<Category> page(Integer pageNum, Integer pageSize, Long parentId) {
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
        Page<Category> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, 0);

        if (parentId != null) {
            wrapper.eq(Category::getParentId, parentId);
        }

        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);

        Page<Category> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    @Override
    public List<Category> listFirstLevel() {
        return baseMapper.selectFirstLevelCategories();
    }

    @Override
    public List<Category> listChildren(Long parentId) {
        return baseMapper.selectByParentId(parentId);
    }

    @Override
    public List<Category> getCategoryTree() {
        try {
            List<Category> cached = redisUtil.get(CACHE_CATEGORY_TREE);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取分类树缓存失败", e);
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1);
        wrapper.eq(Category::getDeleted, 0);
        wrapper.orderByAsc(Category::getSort);

        List<Category> allCategories = baseMapper.selectList(wrapper);

        List<Category> rootCategories = new ArrayList<>();

        for (Category category : allCategories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                rootCategories.add(category);
            }
        }

        try {
            redisUtil.set(CACHE_CATEGORY_TREE, rootCategories, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis写入分类树缓存失败", e);
        }
        return rootCategories;
    }

    private void evictCategoryCache() {
        try {
            redisUtil.delete(CACHE_CATEGORY_TREE);
        } catch (Exception e) {
            log.warn("Redis删除分类树缓存失败", e);
        }
    }

    private Category getEntityById(Long id) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getId, id);
        wrapper.eq(Category::getDeleted, 0);

        Category entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException("分类不存在");
        }
        return entity;
    }
}
