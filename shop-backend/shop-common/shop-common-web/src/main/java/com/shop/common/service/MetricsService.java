package com.shop.common.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 监控指标服务
 *
 * @author shop
 * @since 2026-06-15
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    /**
     * 记录秒杀请求
     */
    public void recordSeckillRequest() {
        Counter.builder("seckill.request.total")
            .description("秒杀请求总数")
            .register(meterRegistry)
            .increment();
    }

    /**
     * 记录秒杀成功
     */
    public void recordSeckillSuccess() {
        Counter.builder("seckill.success.total")
            .description("秒杀成功总数")
            .register(meterRegistry)
            .increment();
    }

    /**
     * 记录秒杀失败
     *
     * @param reason 失败原因
     */
    public void recordSeckillFailure(String reason) {
        Counter.builder("seckill.failure.total")
            .description("秒杀失败总数")
            .tag("reason", reason)
            .register(meterRegistry)
            .increment();
    }

    /**
     * 记录限流触发
     */
    public void recordRateLimitHit() {
        Counter.builder("seckill.ratelimit.hit")
            .description("限流触发次数")
            .register(meterRegistry)
            .increment();
    }

    /**
     * 记录消息处理
     *
     * @param status 处理状态
     */
    public void recordMessageProcess(String status) {
        Counter.builder("seckill.message.process")
            .description("消息处理次数")
            .tag("status", status)
            .register(meterRegistry)
            .increment();
    }

    /**
     * 记录死信消息
     */
    public void recordDeadLetter() {
        Counter.builder("seckill.message.deadletter")
            .description("死信消息数量")
            .register(meterRegistry)
            .increment();
    }

    /**
     * 记录库存回滚
     */
    public void recordStockRollback() {
        Counter.builder("seckill.stock.rollback")
            .description("库存回滚次数")
            .register(meterRegistry)
            .increment();
    }

    /**
     * 记录秒杀耗时
     *
     * @param duration 耗时（毫秒）
     */
    public void recordSeckillDuration(long duration) {
        Timer.builder("seckill.duration")
            .description("秒杀耗时")
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }
}
