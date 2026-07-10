package com.shop.common.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Redis 分布式锁
 * 支持可重入、看门狗续期、Lua 脚本保证原子性
 *
 * @author kaitou
 * @since 2026/06/23
 */
@Slf4j
@Component
public class RedisDistributedLock {

    @Resource(name = "shopRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    private final ScheduledExecutorService watchdog = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "lock-watchdog");
        t.setDaemon(true);
        return t;
    });

    private static final String LOCK_SCRIPT =
            "if redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) then " +
            "  return 1 " +
            "else " +
            "  return 0 " +
            "end";

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";

    private static final String RENEW_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('pexpire', KEYS[1], ARGV[2]) " +
            "else " +
            "  return 0 " +
            "end";

    /**
     * 尝试获取锁
     *
     * @param key       锁的 key
     * @param waitTime  最大等待时间
     * @param leaseTime 锁的持有时间
     * @param unit      时间单位
     * @return LockHandle 用于解锁，如果获取失败返回 null
     */
    public LockHandle tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        String value = UUID.randomUUID().toString();
        long waitMillis = unit.toMillis(waitTime);
        long leaseMillis = unit.toMillis(leaseTime);
        long deadline = System.currentTimeMillis() + waitMillis;

        while (System.currentTimeMillis() < deadline) {
            try {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);
                Long result = redisTemplate.execute(script, Collections.singletonList(key),
                        value, String.valueOf(leaseMillis));
                if (result != null && result == 1L) {
                    // 获取成功，启动看门狗续期
                    AtomicReference<ScheduledFuture<?>> renewalRef = new AtomicReference<>();
                    ScheduledFuture<?> renewal = watchdog.scheduleAtFixedRate(() -> {
                        try {
                            DefaultRedisScript<Long> renewScript = new DefaultRedisScript<>(RENEW_SCRIPT, Long.class);
                            redisTemplate.execute(renewScript, Collections.singletonList(key),
                                    value, String.valueOf(leaseMillis));
                        } catch (Exception e) {
                            log.warn("锁续期失败 key={}: {}", key, e.getMessage());
                        }
                    }, leaseMillis / 3, leaseMillis / 3, TimeUnit.MILLISECONDS);
                    renewalRef.set(renewal);

                    return new LockHandle(key, value, renewalRef, redisTemplate);
                }
            } catch (Exception e) {
                log.error("获取锁异常 key={}: {}", key, e.getMessage());
                return null;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    /**
     * 锁句柄，用于解锁
     */
    public record LockHandle(
            String key,
            String value,
            AtomicReference<ScheduledFuture<?>> renewalRef,
            RedisTemplate<String, Object> redisTemplate
    ) {
        /**
         * 释放锁
         */
        public void unlock() {
            // 取消看门狗续期
            ScheduledFuture<?> renewal = renewalRef.get();
            if (renewal != null) {
                renewal.cancel(false);
            }

            // 释放锁
            try {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
                Long result = redisTemplate.execute(script, Collections.singletonList(key), value);
                if (result != null && result == 1L) {
                    log.debug("锁已释放 key={}", key);
                }
            } catch (Exception e) {
                log.error("释放锁异常 key={}: {}", key, e.getMessage());
            }
        }
    }
}
