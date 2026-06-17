package com.shop.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Redis 限流工具（滑动窗口）
 *
 * @author shop
 * @since 2026-06-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlideWindowRateLimiter {

    private final StringRedisTemplate redisTemplate;

    /**
     * 滑动窗口限流 Lua 脚本
     *
     * KEYS[1]: 限流 key
     * ARGV[1]: 窗口大小（秒）
     * ARGV[2]: 最大请求数
     * ARGV[3]: 当前时间戳（毫秒）
     */
    private static final String RATE_LIMIT_SCRIPT =
        "local key = KEYS[1] " +
        "local window = tonumber(ARGV[1]) " +
        "local limit = tonumber(ARGV[2]) " +
        "local now = tonumber(ARGV[3]) " +
        "local windowStart = now - window * 1000 " +
        "redis.call('zremrangebyscore', key, 0, windowStart) " +
        "local count = redis.call('zcard', key) " +
        "if count < limit then " +
        "  redis.call('zadd', key, now, now .. '-' .. math.random(100000)) " +
        "  redis.call('expire', key, window) " +
        "  return 1 " +
        "else " +
        "  return 0 " +
        "end";

    /**
     * 检查是否允许请求
     *
     * @param key      限流 key
     * @param window   窗口大小（秒）
     * @param limit    最大请求数
     * @return true: 允许, false: 拒绝
     */
    public boolean isAllowed(String key, int window, int limit) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class);
            List<String> keys = Collections.singletonList(key);
            Long result = redisTemplate.execute(script, keys, String.valueOf(window), String.valueOf(limit), String.valueOf(System.currentTimeMillis()));
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Redis 限流检查失败: key={}", key, e);
            // 降级：允许请求
            return true;
        }
    }
}
