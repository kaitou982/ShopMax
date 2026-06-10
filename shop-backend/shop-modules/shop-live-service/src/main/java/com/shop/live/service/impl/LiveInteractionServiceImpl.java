package com.shop.live.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.live.entity.LiveMessage;
import com.shop.live.entity.LiveRoom;
import com.shop.live.mapper.LiveMessageMapper;
import com.shop.live.mapper.LiveRoomMapper;
import com.shop.live.service.LiveInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveInteractionServiceImpl extends ServiceImpl<LiveMessageMapper, LiveMessage> implements LiveInteractionService {

    private final LiveRoomMapper liveRoomMapper;

    @Override
    public void incrementOnlineCount(Long roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room != null) {
            liveRoomMapper.incrementViewCount(roomId);
            // Peak online count tracked via Redis — simplified here
        }
    }

    @Override
    public void decrementOnlineCount(Long roomId) {
        // Online count managed by WebSocket handler via session tracking
    }
}
