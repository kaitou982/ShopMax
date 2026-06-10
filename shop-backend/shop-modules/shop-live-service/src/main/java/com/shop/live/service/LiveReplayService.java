package com.shop.live.service;

/**
 * 直播回放服务接口
 */
public interface LiveReplayService {

    /**
     * 上传录制文件到 MinIO
     *
     * @param roomId    直播间ID
     * @param localPath 本地录制文件路径
     */
    void uploadReplay(Long roomId, String localPath);

    /**
     * 清理过期的回放文件（MinIO 中超过 retention-days 的文件）
     */
    void cleanupExpiredReplays();

    /**
     * 清理本地残留的录制文件（超过 24 小时的文件）
     */
    void cleanupLocalRecordings();
}
