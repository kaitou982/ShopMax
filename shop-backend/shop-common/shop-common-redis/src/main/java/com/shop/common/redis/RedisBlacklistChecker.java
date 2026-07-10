package com.shop.common.redis;

import com.shop.common.security.BlacklistChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 基于 Redis 的 Token 黑名单检查实现
 *
 * @author shop
 * @since 2026-06-23
 */
@Component
@RequiredArgsConstructor
public class RedisBlacklistChecker implements BlacklistChecker {

    private static final String PREFIX = "token:blacklist:";

    private final RedisUtil redisUtil;

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisUtil.hasKey(PREFIX + token));
    }
}
