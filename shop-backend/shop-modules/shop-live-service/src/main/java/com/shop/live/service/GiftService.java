package com.shop.live.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.live.entity.Gift;

import java.util.List;

/**
 * 礼物服务接口
 */
public interface GiftService extends IService<Gift> {

    /**
     * 获取所有可用礼物列表
     */
    List<Gift> listAll();

    /**
     * 发送礼物（扣减虚拟币、记录流水）
     *
     * @param userId  用户ID
     * @param giftId  礼物ID
     * @param count   数量
     * @param roomId  直播间ID
     * @return 消息ID（用于广播）
     */
    Long sendGift(Long userId, Long giftId, Integer count, Long roomId);

    /**
     * 获取用户虚拟币余额
     */
    Integer getCoinBalance(Long userId);

    /**
     * 赠送虚拟币给用户
     */
    void addCoins(Long userId, Integer amount, Integer type, String remark);
}
