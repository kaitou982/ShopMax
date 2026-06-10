package com.shop.product.service;

import com.shop.common.web.PageResult;
import com.shop.product.controller.request.ReviewCreateRequest;
import com.shop.product.entity.ProductReview;

import java.util.Map;

/**
 * 商品评价服务接口
 *
 * @author shop
 * @since 2026-06-07
 */
public interface ProductReviewService {

    /**
     * 创建商品评价
     *
     * @param userId 用户ID
     * @param request 评价请求
     * @return 评价实体
     */
    ProductReview createReview(Long userId, ReviewCreateRequest request);

    /**
     * 分页查询商品评价
     *
     * @param productId 商品ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<ProductReview> getProductReviews(Long productId, Integer pageNum, Integer pageSize);

    /**
     * 查询用户评价
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<ProductReview> getUserReviews(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 删除评价（软删除）
     *
     * @param id 评价ID
     * @param userId 用户ID（校验权限）
     */
    void deleteReview(Long id, Long userId);

    /**
     * 商品评分统计
     *
     * @param productId 商品ID
     * @return 统计信息（avgRating, count）
     */
    Map<String, Object> getReviewStats(Long productId);

    /**
     * 商家回复评价
     *
     * @param id 评价ID
     * @param replyContent 回复内容
     */
    void replyReview(Long id, String replyContent);
}
