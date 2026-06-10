package com.shop.marketing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.CouponCreateRequest;
import com.shop.marketing.controller.request.CouponUpdateRequest;
import com.shop.marketing.controller.response.CouponResponse;
import com.shop.marketing.entity.Coupon;

import java.util.List;
import java.util.Map;

public interface CouponService extends IService<Coupon> {

    CouponResponse create(CouponCreateRequest request);

    CouponResponse update(Long id, CouponUpdateRequest request);

    void delete(Long id);

    CouponResponse getById(Long id);

    PageResult<CouponResponse> page(Integer pageNum, Integer pageSize);

    void receive(Long couponId, Long userId);

    void grantToUser(Long couponId, Long userId);

    void exchangeWithIntegral(Long couponId, Long userId);

    PageResult<CouponResponse> listAvailable();

    Map<String, Object> getStats(Long couponId);

    List<Map<String, Object>> getTrend(Long couponId);
}
