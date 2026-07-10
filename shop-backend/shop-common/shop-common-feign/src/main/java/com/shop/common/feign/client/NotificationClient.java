package com.shop.common.feign.client;

import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 通知服务 FeignClient（供其他微服务调用 admin-service 创建通知）
 */
@FeignClient(name = "shop-admin-service", path = "/api/v1/admin/notifications")
public interface NotificationClient {

    @PostMapping
    Result<Void> createNotification(@RequestBody Map<String, Object> request);
}
