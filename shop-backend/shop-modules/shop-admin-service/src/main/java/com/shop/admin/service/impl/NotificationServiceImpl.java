package com.shop.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.admin.entity.Notification;
import com.shop.admin.mapper.NotificationMapper;
import com.shop.admin.service.NotificationService;
import com.shop.common.web.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public PageResult<Notification> page(Integer pageNum, Integer pageSize, Integer type, Integer isRead) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getDeleted, 0);
        if (type != null) wrapper.eq(Notification::getType, type);
        if (isRead != null) wrapper.eq(Notification::getIsRead, isRead);
        wrapper.orderByDesc(Notification::getCreateTime);

        Page<Notification> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    @Override
    public long getUnreadCount() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getDeleted, 0).eq(Notification::getIsRead, 0);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id).set(Notification::getIsRead, 1);
        baseMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markUnread(Long id) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id).set(Notification::getIsRead, 0);
        baseMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getIsRead, 0).set(Notification::getIsRead, 1);
        baseMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(Integer type, String title, String content, Long refId, String refType) {
        Notification n = new Notification();
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRefId(refId);
        n.setRefType(refType);
        n.setIsRead(0);
        baseMapper.insert(n);
        log.info("创建通知: type={}, title={}", type, title);
    }
}
