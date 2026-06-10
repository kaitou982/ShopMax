package com.shop.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.response.CouponReceiveResponse;
import com.shop.marketing.entity.Coupon;
import com.shop.marketing.entity.CouponReceive;
import com.shop.marketing.mapper.CouponReceiveMapper;
import com.shop.marketing.service.CouponReceiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponReceiveServiceImpl extends ServiceImpl<CouponReceiveMapper, CouponReceive> implements CouponReceiveService {

    private final CouponReceiveMapper couponReceiveMapper;
    private final CouponServiceImpl couponService;

    @Override
    public PageResult<CouponReceiveResponse> pageByUserId(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        Page<CouponReceive> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CouponReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponReceive::getUserId, userId);
        wrapper.eq(CouponReceive::getDeleted, 0);
        if (status != null) {
            wrapper.eq(CouponReceive::getStatus, status);
        }
        wrapper.orderByDesc(CouponReceive::getCreateTime);

        Page<CouponReceive> result = baseMapper.selectPage(page, wrapper);
        List<CouponReceiveResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    public PageResult<CouponReceiveResponse> pageByCouponId(Long couponId, Integer pageNum, Integer pageSize) {
        Page<CouponReceive> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CouponReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponReceive::getCouponId, couponId);
        wrapper.eq(CouponReceive::getDeleted, 0);
        wrapper.orderByDesc(CouponReceive::getCreateTime);

        Page<CouponReceive> result = baseMapper.selectPage(page, wrapper);
        List<CouponReceiveResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long id, Long orderId, String orderNo) {
        CouponReceive receive = getEntityById(id);
        if (receive.getStatus() != 0) {
            throw new BusinessException("优惠券状态不可用");
        }

        Coupon coupon = couponService.getBaseMapper().selectById(receive.getCouponId());
        if (coupon != null) {
            couponService.getBaseMapper().increaseUsedCount(coupon.getId());
        }

        receive.setStatus(1);
        receive.setUseTime(LocalDateTime.now());
        receive.setOrderId(orderId);
        receive.setOrderNo(orderNo);
        baseMapper.updateById(receive);

        log.info("核销优惠券成功: id={}, orderNo={}", id, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireCoupons() {
        LambdaQueryWrapper<CouponReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponReceive::getStatus, 0);
        wrapper.eq(CouponReceive::getDeleted, 0);

        List<CouponReceive> list = baseMapper.selectList(wrapper);
        int count = 0;
        for (CouponReceive receive : list) {
            Coupon coupon = couponService.getBaseMapper().selectById(receive.getCouponId());
            if (coupon != null && coupon.getUseEndTime() != null && coupon.getUseEndTime().isBefore(LocalDateTime.now())) {
                receive.setStatus(2);
                baseMapper.updateById(receive);
                count++;
            }
        }
        log.info("过期优惠券处理完成: 处理{}张", count);
    }

    private CouponReceive getEntityById(Long id) {
        LambdaQueryWrapper<CouponReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponReceive::getId, id);
        wrapper.eq(CouponReceive::getDeleted, 0);

        CouponReceive receive = baseMapper.selectOne(wrapper);
        if (receive == null) {
            throw new BusinessException("优惠券领取记录不存在");
        }
        return receive;
    }

    private CouponReceiveResponse convertToResponse(CouponReceive receive) {
        CouponReceiveResponse response = new CouponReceiveResponse();
        BeanUtils.copyProperties(receive, response);

        Coupon coupon = couponService.getBaseMapper().selectById(receive.getCouponId());
        if (coupon != null) {
            response.setCouponName(coupon.getName());
            response.setCouponType(coupon.getType());
            response.setMinAmount(coupon.getMinAmount());
            response.setDiscountAmount(coupon.getDiscountAmount());
            response.setDiscountRate(coupon.getDiscountRate());
            response.setUseEndTime(coupon.getUseEndTime());
            response.setApplicableType(coupon.getApplicableType());
            response.setApplicableIds(coupon.getApplicableIds());
        }
        return response;
    }
}
