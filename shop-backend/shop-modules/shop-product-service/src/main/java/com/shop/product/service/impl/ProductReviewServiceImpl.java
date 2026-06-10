package com.shop.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.product.controller.request.ReviewCreateRequest;
import com.shop.product.entity.ProductReview;
import com.shop.product.mapper.ProductReviewMapper;
import com.shop.product.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商品评价服务实现
 *
 * @author shop
 * @since 2026-06-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductReview createReview(Long userId, ReviewCreateRequest request) {
        ProductReview review = new ProductReview();
        review.setUserId(userId);
        review.setOrderId(request.getOrderId());
        review.setProductId(request.getProductId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImages(request.getImages());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : 0);
        review.setStatus(1);

        baseMapper.insert(review);
        log.info("创建商品评价成功: userId={}, productId={}, rating={}", userId, request.getProductId(), request.getRating());
        return review;
    }

    @Override
    public PageResult<ProductReview> getProductReviews(Long productId, Integer pageNum, Integer pageSize) {
        Page<ProductReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, productId)
               .eq(ProductReview::getStatus, 1)
               .orderByDesc(ProductReview::getCreateTime);

        Page<ProductReview> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    @Override
    public PageResult<ProductReview> getUserReviews(Long userId, Integer pageNum, Integer pageSize) {
        Page<ProductReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getUserId, userId)
               .eq(ProductReview::getStatus, 1)
               .orderByDesc(ProductReview::getCreateTime);

        Page<ProductReview> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long id, Long userId) {
        ProductReview review = baseMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该评价");
        }

        review.setStatus(0);
        baseMapper.updateById(review);
        log.info("删除商品评价成功: id={}, userId={}", id, userId);
    }

    @Override
    public Map<String, Object> getReviewStats(Long productId) {
        Double avgRating = baseMapper.avgRating(productId);
        int count = baseMapper.countByProduct(productId);
        return Map.of("avgRating", avgRating != null ? avgRating : 0, "count", count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(Long id, String replyContent) {
        ProductReview review = baseMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }

        review.setReplyContent(replyContent);
        review.setReplyTime(LocalDateTime.now());
        baseMapper.updateById(review);
        log.info("商家回复评价成功: id={}", id);
    }
}
