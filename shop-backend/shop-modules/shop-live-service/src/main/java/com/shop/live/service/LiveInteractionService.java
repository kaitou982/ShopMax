package com.shop.live.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.live.entity.LiveMessage;

public interface LiveInteractionService extends IService<LiveMessage> {

    void incrementOnlineCount(Long roomId);

    void decrementOnlineCount(Long roomId);
}
