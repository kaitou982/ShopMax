package com.shop.admin.controller;

import com.shop.admin.service.NotificationService;
import com.shop.common.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知内部接口（供其他微服务通过 Feign 调用，不经过 Gateway 认证）
 */
@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class NotificationInternalController {

    private final NotificationService notificationService;

    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> request) {
        Integer type = request.get("type") != null ? ((Number) request.get("type")).intValue() : null;
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        Long refId = request.get("refId") != null ? ((Number) request.get("refId")).longValue() : null;
        String refType = (String) request.get("refType");
        notificationService.createNotification(type, title, content, refId, refType);
        return Result.success();
    }
}
