package com.shop.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.common.web.PageResult;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shop.product.entity.Product;
import com.shop.product.entity.ProductSku;
import com.shop.product.mapper.ProductMapper;
import com.shop.product.mapper.ProductSkuMapper;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务实现
 *
 * @author shop
 * @since 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String CACHE_PRODUCT_DETAIL = "product:detail:";
    private static final String CACHE_PRODUCT_RECOMMEND = "product:recommend:";
    private static final String CACHE_PRODUCT_NEW = "product:new:";

    private final ProductSkuMapper productSkuMapper;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product create(Product product) {
        // 设置创建者
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            product.setCreateUserId(currentUserId);
        }

        // 设置默认值
        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getSales() == null) {
            product.setSales(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(0);
        }
        if (product.getIsRecommend() == null) {
            product.setIsRecommend(0);
        }
        if (product.getIsNew() == null) {
            product.setIsNew(0);
        }

        baseMapper.insert(product);
        evictProductListCache();
        log.info("创建商品成功: id={}, name={}, createUserId={}", product.getId(), product.getName(), product.getCreateUserId());
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product update(Long id, Product product) {
        Product entity = getEntityById(id);

        // 更新字段
        if (product.getName() != null) {
            entity.setName(product.getName());
        }
        if (product.getSubtitle() != null) {
            entity.setSubtitle(product.getSubtitle());
        }
        if (product.getDescription() != null) {
            entity.setDescription(product.getDescription());
        }
        if (product.getMainImage() != null) {
            entity.setMainImage(product.getMainImage());
        }
        if (product.getSubImages() != null) {
            entity.setSubImages(product.getSubImages());
        }
        if (product.getDetail() != null) {
            entity.setDetail(product.getDetail());
        }
        if (product.getCategoryId() != null) {
            entity.setCategoryId(product.getCategoryId());
        }
        if (product.getBrandId() != null) {
            entity.setBrandId(product.getBrandId());
        }
        if (product.getOriginalPrice() != null) {
            entity.setOriginalPrice(product.getOriginalPrice());
        }
        if (product.getSalePrice() != null) {
            entity.setSalePrice(product.getSalePrice());
        }
        if (product.getStock() != null) {
            entity.setStock(product.getStock());
        }
        if (product.getIsRecommend() != null) {
            entity.setIsRecommend(product.getIsRecommend());
        }
        if (product.getIsNew() != null) {
            entity.setIsNew(product.getIsNew());
        }
        if (product.getSort() != null) {
            entity.setSort(product.getSort());
        }

        baseMapper.updateById(entity);
        evictProductCache(id);
        log.info("更新商品成功: id={}", id);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Product entity = getEntityById(id);
        baseMapper.deleteById(id);

        LambdaUpdateWrapper<ProductSku> skuWrapper = new LambdaUpdateWrapper<>();
        skuWrapper.eq(ProductSku::getProductId, id);
        skuWrapper.set(ProductSku::getDeleted, 1);
        productSkuMapper.update(null, skuWrapper);
        evictProductCache(id);
        log.info("删除商品成功: id={}", id);
    }

    @Override
    public Product getById(Long id) {
        return getEntityById(id);
    }

    @Override
    public PageResult<Product> page(Integer pageNum, Integer pageSize, Long categoryId, String keyword, Integer status, String sortBy) {
        pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0);

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        // STORE 用户只能查看自己的商品
        applyStoreDataFilter(wrapper);

        // 排序
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc" -> wrapper.orderByAsc(Product::getSalePrice);
                case "price_desc" -> wrapper.orderByDesc(Product::getSalePrice);
                case "sales" -> wrapper.orderByDesc(Product::getSales);
                default -> wrapper.orderByDesc(Product::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }

        Page<Product> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onShelf(Long id) {
        Product entity = getEntityById(id);
        entity.setStatus(1);
        baseMapper.updateById(entity);
        evictProductCache(id);
        log.info("商品上架成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offShelf(Long id) {
        Product entity = getEntityById(id);
        entity.setStatus(0);
        baseMapper.updateById(entity);
        evictProductCache(id);
        log.info("商品下架成功: id={}", id);
    }

    @Override
    public List<Product> listRecommend(Integer limit) {
        String cacheKey = buildListCacheKey(CACHE_PRODUCT_RECOMMEND, limit);
        try {
            List<Product> cached = redisUtil.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取推荐缓存失败", e);
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.eq(Product::getDeleted, 0);
        wrapper.eq(Product::getIsRecommend, 1);
        applyStoreDataFilter(wrapper);
        wrapper.orderByDesc(Product::getSales);
        wrapper.last("LIMIT " + limit);

        List<Product> result = baseMapper.selectList(wrapper);
        try {
            redisUtil.set(cacheKey, result, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis写入推荐缓存失败", e);
        }
        return result;
    }

    @Override
    public List<Product> listNew(Integer limit) {
        String cacheKey = buildListCacheKey(CACHE_PRODUCT_NEW, limit);
        try {
            List<Product> cached = redisUtil.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取新品缓存失败", e);
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.eq(Product::getDeleted, 0);
        wrapper.eq(Product::getIsNew, 1);
        applyStoreDataFilter(wrapper);
        wrapper.orderByDesc(Product::getCreateTime);
        wrapper.last("LIMIT " + limit);

        List<Product> result = baseMapper.selectList(wrapper);
        try {
            redisUtil.set(cacheKey, result, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis写入新品缓存失败", e);
        }
        return result;
    }

    private String buildListCacheKey(String prefix, Integer limit) {
        if (isStoreUser()) {
            return prefix + "store:" + getCurrentUserId() + ":" + limit;
        }
        return prefix + limit;
    }

    private Product getEntityById(Long id) {
        String cacheKey = CACHE_PRODUCT_DETAIL + id;
        try {
            Product cached = redisUtil.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取商品缓存失败: id={}", id, e);
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getId, id);
        wrapper.eq(Product::getDeleted, 0);

        Product entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException("商品不存在");
        }

        try {
            redisUtil.set(cacheKey, entity, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis写入商品缓存失败: id={}", id, e);
        }
        return entity;
    }

    private void evictProductCache(Long id) {
        try {
            redisUtil.delete(CACHE_PRODUCT_DETAIL + id);
            evictProductListCache();
        } catch (Exception e) {
            log.warn("Redis删除商品缓存失败: id={}", id, e);
        }
    }

    private void evictProductListCache() {
        try {
            redisUtil.delete(CACHE_PRODUCT_RECOMMEND);
            redisUtil.delete(CACHE_PRODUCT_NEW);
        } catch (Exception e) {
            log.warn("Redis删除商品列表缓存失败", e);
        }
    }

    /**
     * 获取当前登录用户ID（从 SecurityContext）
     */
    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    /**
     * 判断当前用户是否为 STORE 角色
     */
    private boolean isStoreUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STORE"));
    }

    /**
     * STORE 用户数据隔离：仅查看自己的商品
     */
    private void applyStoreDataFilter(LambdaQueryWrapper<Product> wrapper) {
        if (isStoreUser()) {
            Long userId = getCurrentUserId();
            wrapper.eq(Product::getCreateUserId, userId);
        }
    }
}
