package com.shop.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.redis.RedisUtil;
import com.shop.common.security.jwt.JwtUtil;
import com.shop.live.entity.LiveRoom;
import com.shop.live.mapper.LiveRoomMapper;
import com.shop.live.service.SrsCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * SRS 回调服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SrsCallbackServiceImpl implements SrsCallbackService {

    private final LiveRoomMapper liveRoomMapper;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    /** 推流白名单 Redis Key 前缀 */
    private static final String PUSH_WHITELIST_KEY = "live:push:whitelist:";
    /** 直播状态 Redis Key 前缀 */
    private static final String ROOM_STATUS_KEY = "live:room:status:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleCallback(String app, String stream, String action, String param) {
        try {
            Long roomId = Long.parseLong(stream);

            if ("on_publish".equals(action)) {
                return handleOnPublish(roomId, param);
            } else if ("on_unpublish".equals(action)) {
                return handleOnUnpublish(roomId);
            }

            return "0";
        } catch (Exception e) {
            log.error("处理SRS回调失败: action={}, stream={}, error={}", action, stream, e.getMessage(), e);
            return "0";
        }
    }

    @Override
    public String verifyPlayAccess(String stream, String param) {
        try {
            Long roomId = Long.parseLong(stream);

            // 从 param 中提取 token
            String token = extractTokenFromParam(param);
            if (token == null || token.isEmpty()) {
                log.warn("播放鉴权失败: 缺少token, roomId={}", roomId);
                return "1"; // 拒绝
            }

            // 验证 token 是否有效
            if (!jwtUtil.validateToken(token)) {
                log.warn("播放鉴权失败: token无效, roomId={}", roomId);
                return "1"; // 拒绝
            }

            log.info("播放鉴权成功: roomId={}", roomId);
            return "0"; // 允许
        } catch (Exception e) {
            log.error("播放鉴权异常: stream={}, error={}", stream, e.getMessage());
            return "1"; // 拒绝
        }
    }

    /**
     * 处理推流开始回调
     */
    private String handleOnPublish(Long roomId, String param) {
        // 1. 查询直播间
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            log.error("推流回调: 直播间不存在, roomId={}", roomId);
            return "1";
        }

        // 2. 如果房间已经是直播中状态（SRS 重连场景），直接允许
        if (room.getStatus() == 1) {
            log.info("推流重连: roomId={}, 房间已是直播中状态, 允许推流", roomId);
            return "0";
        }

        // 3. 验证推流白名单（首次推流）
        String whitelistKey = PUSH_WHITELIST_KEY + roomId;
        String storedToken = redisUtil.get(whitelistKey);

        if (storedToken == null) {
            log.warn("推流鉴权失败: 白名单中无此roomId, roomId={}", roomId);
            return "1"; // 拒绝推流
        }

        // 4. 从 param 中提取 token 并验证
        String token = extractTokenFromParam(param);
        if (token != null && !token.equals(storedToken)) {
            log.warn("推流鉴权失败: token不匹配, roomId={}", roomId);
            return "1"; // 拒绝推流
        }

        // 5. 更新房间状态为"直播中"
        room.setStatus(1); // 直播中
        room.setActualStartTime(LocalDateTime.now());
        liveRoomMapper.updateById(room);

        // 6. 更新 Redis 状态
        redisUtil.set(ROOM_STATUS_KEY + roomId, 1);

        // 7. 清除白名单（已验证通过）
        redisUtil.delete(whitelistKey);

        log.info("推流开始: roomId={}, 状态已更新为直播中", roomId);
        return "0"; // 允许推流
    }

    /**
     * 处理推流结束回调
     */
    private String handleOnUnpublish(Long roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            log.error("断流回调: 直播间不存在, roomId={}", roomId);
            return "0";
        }

        // 只有直播中状态才更新
        if (room.getStatus() != 1) {
            log.info("断流回调: 房间非直播中状态, roomId={}, status={}", roomId, room.getStatus());
            return "0";
        }

        // 更新房间状态为"已结束"
        room.setStatus(2); // 已结束
        room.setEndTime(LocalDateTime.now());
        if (room.getActualStartTime() != null) {
            long duration = Duration.between(room.getActualStartTime(), room.getEndTime()).getSeconds();
            room.setDuration(duration);
        }
        liveRoomMapper.updateById(room);

        // 更新 Redis 状态
        redisUtil.set(ROOM_STATUS_KEY + roomId, 2);

        log.info("推流结束: roomId={}, 时长={}秒", roomId, room.getDuration());

        // TODO: 触发录制文件上传到 MinIO（异步任务）
        // liveReplayService.uploadReplay(roomId);

        return "0";
    }

    /**
     * 从 SRS 回调参数中提取 token
     * SRS 回调的 param 格式: "?token=xxx" 或 "token=xxx&other=yyy"
     */
    private String extractTokenFromParam(String param) {
        if (param == null || param.isEmpty()) {
            return null;
        }

        // 去除前导 ?
        if (param.startsWith("?")) {
            param = param.substring(1);
        }

        String[] pairs = param.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }
}
