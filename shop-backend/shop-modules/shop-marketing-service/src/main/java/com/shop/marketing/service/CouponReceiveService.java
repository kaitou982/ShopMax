package com.shop.marketing.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.response.CouponReceiveResponse;
import com.shop.marketing.entity.CouponReceive;

public interface CouponReceiveService extends IService<CouponReceive> {

    PageResult<CouponReceiveResponse> pageByUserId(Long userId, Integer pageNum, Integer pageSize, Integer status);

    PageResult<CouponReceiveResponse> pageByCouponId(Long couponId, Integer pageNum, Integer pageSize);

    void useCoupon(Long id, Long orderId, String orderNo);

    void expireCoupons();
}
