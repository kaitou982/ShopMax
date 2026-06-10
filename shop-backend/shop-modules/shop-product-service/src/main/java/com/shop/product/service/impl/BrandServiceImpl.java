package com.shop.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.common.web.PageResult;
import com.shop.product.entity.Brand;
import com.shop.product.mapper.BrandMapper;
import com.shop.product.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 品牌服务实现
 *
 * @author shop
 * @since 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CACHE_BRAND_LIST = "brand:list";

    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Brand create(Brand brand) {
        baseMapper.insert(brand);
        evictBrandCache();
        log.info("创建品牌成功: id={}, name={}", brand.getId(), brand.getName());
        return brand;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Brand update(Long id, Brand brand) {
        Brand entity = getEntityById(id);

        if (brand.getName() != null) {
            entity.setName(brand.getName());
        }
        if (brand.getLogo() != null) {
            entity.setLogo(brand.getLogo());
        }
        if (brand.getDescription() != null) {
            entity.setDescription(brand.getDescription());
        }
        if (brand.getSort() != null) {
            entity.setSort(brand.getSort());
        }
        if (brand.getStatus() != null) {
            entity.setStatus(brand.getStatus());
        }

        baseMapper.updateById(entity);
        evictBrandCache();
        log.info("更新品牌成功: id={}", id);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Brand entity = getEntityById(id);
        baseMapper.deleteById(id);
        evictBrandCache();
        log.info("删除品牌成功: id={}", id);
    }

    @Override
    public Brand getById(Long id) {
        return getEntityById(id);
    }

    @Override
    public PageResult<Brand> page(Integer pageNum, Integer pageSize) {
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
        Page<Brand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getDeleted, 0);
        wrapper.orderByAsc(Brand::getSort).orderByDesc(Brand::getCreateTime);

        Page<Brand> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    @Override
    public List<Brand> listAll() {
        try {
            List<Brand> cached = redisUtil.get(CACHE_BRAND_LIST);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取品牌列表缓存失败", e);
        }

        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getStatus, 1);
        wrapper.eq(Brand::getDeleted, 0);
        wrapper.orderByAsc(Brand::getSort);

        List<Brand> result = baseMapper.selectList(wrapper).stream()
                .collect(java.util.stream.Collectors.toMap(
                        Brand::getName, b -> b, (a, b) -> a, java.util.LinkedHashMap::new))
                .values().stream().collect(java.util.stream.Collectors.toList());
        try {
            redisUtil.set(CACHE_BRAND_LIST, result, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis写入品牌列表缓存失败", e);
        }
        return result;
    }

    private void evictBrandCache() {
        try {
            redisUtil.delete(CACHE_BRAND_LIST);
        } catch (Exception e) {
            log.warn("Redis删除品牌列表缓存失败", e);
        }
    }

    private Brand getEntityById(Long id) {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getId, id);
        wrapper.eq(Brand::getDeleted, 0);

        Brand entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException("品牌不存在");
        }
        return entity;
    }
}
