package com.shop.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.marketing.controller.request.CouponCreateRequest;
import com.shop.marketing.controller.request.CouponUpdateRequest;
import com.shop.marketing.controller.response.CouponResponse;
import com.shop.marketing.entity.Coupon;
import com.shop.marketing.entity.CouponReceive;
import com.shop.marketing.mapper.CouponMapper;
import com.shop.marketing.mapper.CouponReceiveMapper;
import com.shop.marketing.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CouponMapper couponMapper;
    private final CouponReceiveMapper couponReceiveMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponResponse create(CouponCreateRequest request) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(request, coupon);
        coupon.setReceivedCount(0);
        coupon.setUsedCount(0);

        baseMapper.insert(coupon);
        log.info("创建优惠券成功: id={}, name={}", coupon.getId(), coupon.getName());
        return convertToResponse(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponResponse update(Long id, CouponUpdateRequest request) {
        Coupon coupon = getEntityById(id);
        BeanUtils.copyProperties(request, coupon);

        baseMapper.updateById(coupon);
        log.info("更新优惠券成功: id={}", id);
        return convertToResponse(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Coupon coupon = getEntityById(id);
        baseMapper.deleteById(id);
        log.info("删除优惠券成功: id={}", id);
    }

    @Override
    public CouponResponse getById(Long id) {
        return convertToResponse(getEntityById(id));
    }

    @Override
    public PageResult<CouponResponse> page(Integer pageNum, Integer pageSize) {
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getDeleted, 0);
        wrapper.orderByDesc(Coupon::getCreateTime);

        Page<Coupon> result = baseMapper.selectPage(page, wrapper);
        List<CouponResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long couponId, Long userId) {
        Coupon coupon = getEntityById(couponId);

        if (coupon.getStatus() != null && coupon.getStatus() == 0) {
            throw new BusinessException("该优惠券已禁用");
        }

        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BusinessException("优惠券已被领完");
        }

        int userCount = couponReceiveMapper.countByCouponIdAndUserId(couponId, userId);
        if (coupon.getPerLimit() != null && coupon.getPerLimit() > 0
                && userCount >= coupon.getPerLimit()) {
            throw new BusinessException("已达领取上限");
        }

        CouponReceive receive = new CouponReceive();
        receive.setCouponId(couponId);
        receive.setUserId(userId);
        receive.setReceiveTime(LocalDateTime.now());
        receive.setStatus(0);
        try {
            couponReceiveMapper.insert(receive);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("已领取该优惠券");
        }

        int updated = couponMapper.increaseReceivedCount(couponId);
        if (updated == 0) {
            throw new BusinessException("优惠券已被领完");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantToUser(Long couponId, Long userId) {
        Coupon coupon = baseMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在: couponId=" + couponId);
        }

        CouponReceive receive = new CouponReceive();
        receive.setCouponId(couponId);
        receive.setUserId(userId);
        receive.setReceiveTime(LocalDateTime.now());
        receive.setStatus(0);
        couponReceiveMapper.insert(receive);

        couponMapper.increaseReceivedCount(couponId);
        log.info("管理员发放优惠券: couponId={}, userId={}", couponId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeWithIntegral(Long couponId, Long userId) {
        Coupon coupon = getEntityById(couponId);

        if (coupon.getIntegralCost() == null || coupon.getIntegralCost() <= 0) {
            throw new BusinessException("该优惠券不支持积分兑换");
        }

        if (coupon.getStatus() != null && coupon.getStatus() == 0) {
            throw new BusinessException("该优惠券已禁用");
        }

        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BusinessException("优惠券已被兑换完");
        }

        // Check user integral
        String sql = "SELECT integral FROM ums_user WHERE id = ? AND deleted = 0";
        Integer integral = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        if (integral == null || integral < coupon.getIntegralCost()) {
            throw new BusinessException("积分不足");
        }

        // Deduct integral
        jdbcTemplate.update("UPDATE ums_user SET integral = integral - ? WHERE id = ?",
                coupon.getIntegralCost(), userId);

        // Create receive record
        CouponReceive receive = new CouponReceive();
        receive.setCouponId(couponId);
        receive.setUserId(userId);
        receive.setReceiveTime(LocalDateTime.now());
        receive.setStatus(0);
        try {
            couponReceiveMapper.insert(receive);
        } catch (DuplicateKeyException e) {
            // Rollback integral deduction
            jdbcTemplate.update("UPDATE ums_user SET integral = integral + ? WHERE id = ?",
                    coupon.getIntegralCost(), userId);
            throw new BusinessException("已兑换该优惠券");
        }

        // Increase received count
        int updated = couponMapper.increaseReceivedCount(couponId);
        if (updated == 0) {
            throw new BusinessException("优惠券已被兑换完");
        }

        log.info("积分兑换优惠券: userId={}, couponId={}, integral={}", userId, couponId, coupon.getIntegralCost());
    }

    @Override
    public PageResult<CouponResponse> listAvailable() {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getDeleted, 0);
        wrapper.eq(Coupon::getStatus, 1);
        wrapper.apply("received_count < total_count");
        wrapper.orderByDesc(Coupon::getCreateTime);

        List<Coupon> list = baseMapper.selectList(wrapper);
        List<CouponResponse> records = list.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(records, (long) records.size(), 1L);
    }

    private Coupon getEntityById(Long id) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getId, id);
        wrapper.eq(Coupon::getDeleted, 0);

        Coupon coupon = baseMapper.selectOne(wrapper);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        return coupon;
    }

    @Override
    public Map<String, Object> getStats(Long couponId) {
        Coupon coupon = getEntityById(couponId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("id", coupon.getId());
        stats.put("name", coupon.getName());
        stats.put("totalCount", coupon.getTotalCount());
        stats.put("receivedCount", coupon.getReceivedCount());
        stats.put("usedCount", coupon.getUsedCount());
        stats.put("remainCount", coupon.getTotalCount() - coupon.getReceivedCount());
        stats.put("usageRate", coupon.getReceivedCount() > 0
                ? Math.round(coupon.getUsedCount() * 100.0 / coupon.getReceivedCount()) : 0);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getTrend(Long couponId) {
        LambdaQueryWrapper<CouponReceive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponReceive::getCouponId, couponId);
        wrapper.select(CouponReceive::getReceiveTime);
        wrapper.orderByAsc(CouponReceive::getReceiveTime);
        List<CouponReceive> records = couponReceiveMapper.selectList(wrapper);

        Map<String, Long> grouped = records.stream()
                .filter(r -> r.getReceiveTime() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getReceiveTime().toLocalDate().toString(),
                        Collectors.counting()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private CouponResponse convertToResponse(Coupon coupon) {
        CouponResponse response = new CouponResponse();
        BeanUtils.copyProperties(coupon, response);
        return response;
    }
}
