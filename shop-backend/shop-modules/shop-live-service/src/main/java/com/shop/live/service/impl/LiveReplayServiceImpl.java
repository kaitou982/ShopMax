package com.shop.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.storage.service.StorageService;
import com.shop.live.entity.LiveRoom;
import com.shop.live.mapper.LiveRoomMapper;
import com.shop.live.service.LiveReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 直播回放服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveReplayServiceImpl implements LiveReplayService {

    private final StorageService storageService;
    private final LiveRoomMapper liveRoomMapper;

    @Value("${live.replay.retention-days:30}")
    private int retentionDays;

    @Value("${live.replay.record-path:./docker/srs/record}")
    private String recordPath;

    @Override
    @Async("liveReplayExecutor")
    public void uploadReplay(Long roomId, String localPath) {
        try {
            Path filePath = Paths.get(localPath);
            if (!Files.exists(filePath)) {
                log.warn("录制文件不存在: {}", localPath);
                return;
            }

            // 生成 MinIO 对象名称
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String objectName = "live-replay/room-" + roomId + "/" + timestamp + ".flv";

            // 上传到 MinIO
            try (FileInputStream fis = new FileInputStream(localPath)) {
                String url = storageService.upload(objectName, fis, "video/x-flv", Files.size(filePath));

                // 更新数据库
                LiveRoom room = liveRoomMapper.selectById(roomId);
                if (room != null) {
                    room.setReplayUrl(url);
                    liveRoomMapper.updateById(room);
                }

                log.info("录制文件上传成功: roomId={}, url={}", roomId, url);
            }

            // 删除本地文件
            Files.deleteIfExists(filePath);
            log.info("本地录制文件已删除: {}", localPath);

        } catch (IOException e) {
            log.error("录制文件处理失败: roomId={}, error={}", roomId, e.getMessage(), e);
        }
    }

    @Override
    public void cleanupExpiredReplays() {
        // TODO: 实现 MinIO 中过期回放文件的清理
        // 需要列出 live-replay/ 前缀下的所有对象，检查创建时间，删除超过 retentionDays 的文件
        log.info("清理过期回放文件: retentionDays={}", retentionDays);
    }

    @Override
    public void cleanupLocalRecordings() {
        try {
            Path recordDir = Paths.get(recordPath);
            if (!Files.exists(recordDir)) {
                return;
            }

            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

            Files.walk(recordDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".flv"))
                    .forEach(path -> {
                        try {
                            // 从文件名提取时间戳
                            String fileName = path.getFileName().toString();
                            // 文件名格式: live.{timestamp}.flv
                            if (fileName.startsWith("live.") && fileName.endsWith(".flv")) {
                                String timestampStr = fileName.substring(5, fileName.length() - 4);
                                LocalDateTime fileTime = LocalDateTime.parse(timestampStr, formatter);
                                if (fileTime.isBefore(cutoff)) {
                                    Files.delete(path);
                                    log.info("删除过期本地录制文件: {}", path);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("处理录制文件失败: {}, error={}", path, e.getMessage());
                        }
                    });

            log.info("本地录制文件清理完成");
        } catch (IOException e) {
            log.error("清理本地录制文件失败: {}", e.getMessage(), e);
        }
    }
}
