package com.shop.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.user.entity.BalanceLog;
import com.shop.user.entity.IntegralLog;
import com.shop.user.entity.User;
import com.shop.user.mapper.BalanceLogMapper;
import com.shop.user.mapper.IntegralLogMapper;
import com.shop.user.mapper.UserMapper;
import com.shop.user.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final UserMapper userMapper;
    private final IntegralLogMapper integralLogMapper;
    private final BalanceLogMapper balanceLogMapper;

    private static final int[] LEVEL_THRESHOLDS = {0, 500, 2000, 10000}; // 普通/银卡/金卡/钻石
    private static final double[] LEVEL_DISCOUNTS = {1.0, 0.98, 0.95, 0.90};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeIntegral(Long userId, int amount, int type, String bizId, String remark) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        int newVal = user.getIntegral() + amount;
        if (newVal < 0) throw new BusinessException("积分不足");

        IntegralLog ilog = new IntegralLog();
        ilog.setUserId(userId);
        ilog.setChangeAmount(amount);
        ilog.setAfterAmount(newVal);
        ilog.setType(type);
        ilog.setBizId(bizId);
        ilog.setRemark(remark);
        ilog.setCreateTime(LocalDateTime.now());
        integralLogMapper.insert(ilog);

        user.setIntegral(newVal);
        userMapper.updateById(user);
        log.info("积分变动: userId={}, amount={}, after={}, type={}", userId, amount, newVal, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeBalance(Long userId, BigDecimal amount, int type, String bizId, String payChannel, String remark) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        BigDecimal newVal = user.getBalance().add(amount);
        if (newVal.compareTo(BigDecimal.ZERO) < 0) throw new BusinessException("余额不足");

        BalanceLog blog = new BalanceLog();
        blog.setUserId(userId);
        blog.setChangeAmount(amount);
        blog.setAfterAmount(newVal);
        blog.setType(type);
        blog.setBizId(bizId);
        blog.setPayChannel(payChannel);
        blog.setRemark(remark);
        blog.setCreateTime(LocalDateTime.now());
        balanceLogMapper.insert(blog);

        user.setBalance(newVal);
        userMapper.updateById(user);
        log.info("余额变动: userId={}, amount={}, after={}, type={}", userId, amount, newVal, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGrowthValue(Long userId, int growth) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        int newGrowth = user.getGrowthValue() + growth;
        int oldLevel = user.getMemberLevel();
        int newLevel = oldLevel;

        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (newGrowth >= LEVEL_THRESHOLDS[i]) {
                newLevel = i + 1;
                break;
            }
        }

        user.setGrowthValue(newGrowth);
        user.setMemberLevel(newLevel);
        userMapper.updateById(user);

        if (newLevel > oldLevel) {
            log.info("会员升级: userId={}, {}→{}级, growth={}", userId, oldLevel, newLevel, newGrowth);
        }
    }

    @Override
    public PageResult<IntegralLog> integralLogs(Long userId, int pageNum, int pageSize) {
        Page<IntegralLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IntegralLog> w = new LambdaQueryWrapper<>();
        w.eq(IntegralLog::getUserId, userId).orderByDesc(IntegralLog::getCreateTime);
        Page<IntegralLog> result = integralLogMapper.selectPage(page, w);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    @Override
    public PageResult<BalanceLog> balanceLogs(Long userId, int pageNum, int pageSize) {
        Page<BalanceLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BalanceLog> w = new LambdaQueryWrapper<>();
        w.eq(BalanceLog::getUserId, userId).orderByDesc(BalanceLog::getCreateTime);
        Page<BalanceLog> result = balanceLogMapper.selectPage(page, w);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }
}
