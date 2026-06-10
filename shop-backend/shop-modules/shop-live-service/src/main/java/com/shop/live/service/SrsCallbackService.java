package com.shop.live.service;

/**
 * SRS 回调服务接口
 */
public interface SrsCallbackService {

    /**
     * 处理 SRS 推流/断流回调
     *
     * @param app    应用名（如 "live"）
     * @param stream 流名（即 roomId）
     * @param action 动作（"on_publish" 或 "on_unpublish"）
     * @param param  附加参数（可能包含 token）
     * @return "0" 表示允许，非 "0" 表示拒绝
     */
    String handleCallback(String app, String stream, String action, String param);

    /**
     * 验证观众播放权限
     *
     * @param stream 流名（即 roomId）
     * @param param  附加参数（可能包含 token）
     * @return "0" 表示允许播放，非 "0" 表示拒绝
     */
    String verifyPlayAccess(String stream, String param);
}
