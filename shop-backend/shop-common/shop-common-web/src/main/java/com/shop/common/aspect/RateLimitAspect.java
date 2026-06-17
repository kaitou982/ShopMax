package com.shop.common.aspect;

import com.google.common.util.concurrent.RateLimiter;
import com.shop.common.annotation.RateLimit;
import com.shop.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面
 *
 * @author shop
 * @since 2026-06-15
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final ConcurrentHashMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.key();
        if (key.isEmpty()) {
            key = point.getSignature().toShortString();
        }

        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(key, k ->
            RateLimiter.create(rateLimit.permitsPerSecond())
        );

        if (!rateLimiter.tryAcquire()) {
            log.warn("限流触发: key={}", key);
            throw new BusinessException(rateLimit.message());
        }

        return point.proceed();
    }
}
