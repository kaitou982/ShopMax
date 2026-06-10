package com.shop.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.live.entity.CoinLog;
import com.shop.live.entity.Gift;
import com.shop.live.entity.LiveMessage;
import com.shop.live.mapper.CoinLogMapper;
import com.shop.live.mapper.GiftMapper;
import com.shop.live.mapper.LiveMessageMapper;
import com.shop.live.service.GiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 礼物服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GiftServiceImpl extends ServiceImpl<GiftMapper, Gift> implements GiftService {

    private final GiftMapper giftMapper;
    private final CoinLogMapper coinLogMapper;
    private final LiveMessageMapper messageMapper;
    private final RedisUtil redisUtil;

    /** 虚拟币余额 Redis Key 前缀 */
    private static final String COIN_BALANCE_KEY = "live:coin:";

    @Override
    public List<Gift> listAll() {
        LambdaQueryWrapper<Gift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Gift::getDeleted, 0);
        wrapper.orderByAsc(Gift::getSortOrder);
        return giftMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendGift(Long userId, Long giftId, Integer count, Long roomId) {
        // 1. 获取礼物信息
        Gift gift = giftMapper.selectById(giftId);
        if (gift == null || gift.getDeleted() == 1) {
            throw new BusinessException("礼物不存在");
        }

        // 2. 计算总花费
        int totalCost = gift.getPrice() * count;

        // 3. 检查余额
        Integer balance = getCoinBalance(userId);
        if (balance < totalCost) {
            throw new BusinessException("虚拟币余额不足");
        }

        // 4. 扣减余额（Redis 原子操作）
        String balanceKey = COIN_BALANCE_KEY + userId;
        redisUtil.decrement(balanceKey, totalCost);

        // 5. 记录流水
        CoinLog coinLog = new CoinLog();
        coinLog.setUserId(userId);
        coinLog.setAmount(-totalCost);
        coinLog.setType(3); // 送礼消费
        coinLog.setRemark("送出 " + count + " 个" + gift.getName());
        coinLogMapper.insert(coinLog);

        // 6. 记录直播消息
        LiveMessage message = new LiveMessage();
        message.setRoomId(roomId);
        message.setUserId(userId);
        message.setType(3); // 礼物
        message.setGiftId(giftId);
        message.setGiftCount(count);
        message.setContent(gift.getName());
        messageMapper.insert(message);

        log.info("用户 {} 送出 {} 个{}, 花费 {} 币", userId, count, gift.getName(), totalCost);
        return message.getId();
    }

    @Override
    public Integer getCoinBalance(Long userId) {
        String balanceKey = COIN_BALANCE_KEY + userId;
        Object balance = redisUtil.get(balanceKey);

        if (balance != null) {
            if (balance instanceof Integer intVal) {
                return intVal;
            }
            if (balance instanceof String strVal) {
                return Integer.parseInt(strVal);
            }
        }

        // 如果 Redis 中没有，从数据库加载（需要查询 ums_user 表）
        // 这里简化处理，返回默认值 100
        // 实际应该查询 ums_user 表的 coin_balance 字段
        redisUtil.set(balanceKey, 100, 24, TimeUnit.HOURS);
        return 100;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCoins(Long userId, Integer amount, Integer type, String remark) {
        // 1. 增加余额
        String balanceKey = COIN_BALANCE_KEY + userId;
        redisUtil.increment(balanceKey, amount);

        // 2. 记录流水
        CoinLog coinLog = new CoinLog();
        coinLog.setUserId(userId);
        coinLog.setAmount(amount);
        coinLog.setType(type);
        coinLog.setRemark(remark);
        coinLogMapper.insert(coinLog);

        log.info("用户 {} 获得 {} 币, 原因: {}", userId, amount, remark);
    }
}
