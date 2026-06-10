package com.shop.live.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 直播回放异步任务配置
 */
@Configuration
@EnableAsync
@EnableScheduling
public class LiveReplayConfig {

    /**
     * 直播回放异步任务线程池
     */
    @Bean("liveReplayExecutor")
    public Executor liveReplayExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("live-replay-");
        executor.initialize();
        return executor;
    }
}
