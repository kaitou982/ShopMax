package com.shop.admin.controller;

import com.shop.admin.entity.Notification;
import com.shop.admin.service.NotificationService;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "分页查询通知")
    @GetMapping
    public Result<PageResult<Notification>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead) {
        return Result.success(notificationService.page(pageNum, pageSize, type, isRead));
    }

    @Operation(summary = "获取未读数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }

    @Operation(summary = "标记单条已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    @Operation(summary = "标记单条未读")
    @PutMapping("/{id}/unread")
    public Result<Void> markUnread(@PathVariable Long id) {
        notificationService.markUnread(id);
        return Result.success();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.success();
    }

    @Operation(summary = "创建通知")
    @PostMapping
    public Result<Void> create(@RequestBody Notification request) {
        notificationService.createNotification(
                request.getType(), request.getTitle(), request.getContent(),
                request.getRefId(), request.getRefType());
        return Result.success();
    }
}
