package com.shop.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.admin.entity.Notification;
import com.shop.common.web.PageResult;

public interface NotificationService extends IService<Notification> {

    PageResult<Notification> page(Integer pageNum, Integer pageSize, Integer type, Integer isRead);

    long getUnreadCount();

    void markRead(Long id);

    void markAllRead();

    void createNotification(Integer type, String title, String content, Long refId, String refType);
}
