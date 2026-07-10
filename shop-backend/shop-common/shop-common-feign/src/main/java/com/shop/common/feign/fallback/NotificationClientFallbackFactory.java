package com.shop.common.feign.fallback;

import com.shop.common.feign.client.NotificationClient;
import com.shop.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通知服务 FeignClient 降级工厂
 */
@Slf4j
@Component
public class NotificationClientFallbackFactory implements FallbackFactory<NotificationClient> {

    @Override
    public NotificationClient create(Throwable cause) {
        log.error("通知服务调用失败: {}", cause.getMessage(), cause);
        return new NotificationClient() {
            @Override
            public Result<Void> createNotification(Map<String, Object> request) {
                return Result.error(503, "通知服务暂时不可用，请稍后再试");
            }
        };
    }
}
