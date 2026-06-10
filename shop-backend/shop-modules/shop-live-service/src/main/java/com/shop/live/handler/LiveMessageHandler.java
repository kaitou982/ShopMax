package com.shop.live.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.live.entity.Gift;
import com.shop.live.entity.LiveMessage;
import com.shop.live.mapper.LiveMessageMapper;
import com.shop.live.mapper.LiveRoomMapper;
import com.shop.live.service.GiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveMessageHandler {

    private final LiveRoomMapper liveRoomMapper;
    private final LiveMessageMapper messageMapper;
    private final RedisUtil redisUtil;
    private final GiftService giftService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LIKE_COUNT_KEY = "live:like:";
    private static final String DANMAKU_RATE_KEY = "live:danmaku:rate:";

    @SuppressWarnings("unchecked")
    public Map<String, Object> handleMessage(Long roomId, WebSocketSession session, String payload) {
        try {
            Map<String, Object> msg = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            String type = (String) msg.get("type");

            switch (type) {
                case "danmaku":
                    return handleDanmaku(roomId, session, msg);
                case "like":
                    return handleLike(roomId, session, msg);
                case "gift":
                    return handleGift(roomId, session, msg);
                default:
                    log.warn("未知消息类型: {}", type);
                    return null;
            }
        } catch (Exception e) {
            log.error("解析消息失败: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> handleDanmaku(Long roomId, WebSocketSession session, Map<String, Object> msg) {
        String content = (String) msg.get("content");
        if (content == null || content.trim().isEmpty()) return null;
        if (content.length() > 200) content = content.substring(0, 200);

        // Rate limiting: 1 danmaku per 2 seconds per user per room
        Long userId = parseUserId(msg);
        String rateKey = DANMAKU_RATE_KEY + roomId + ":" + userId;
        if (Boolean.TRUE.equals(redisUtil.hasKey(rateKey))) return null;
        redisUtil.set(rateKey, "1", 2, TimeUnit.SECONDS);

        // Filter sensitive words (simple implementation)
        content = filterSensitiveWords(content);

        // Save to DB
        LiveMessage message = new LiveMessage();
        message.setRoomId(roomId);
        message.setUserId(userId);
        message.setType(1);
        message.setContent(content);
        messageMapper.insert(message);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "danmaku");
        response.put("userId", userId);
        response.put("nickname", msg.getOrDefault("nickname", "用户"));
        response.put("content", content);
        return response;
    }

    private Map<String, Object> handleLike(Long roomId, WebSocketSession session, Map<String, Object> msg) {
        Long userId = parseUserId(msg);

        // Increment like count in Redis
        String key = LIKE_COUNT_KEY + roomId;
        Long totalLikes = redisUtil.increment(key, 1);

        // Async persist to DB (every 10 likes)
        if (totalLikes % 10 == 0) {
            LiveMessage message = new LiveMessage();
            message.setRoomId(roomId);
            message.setUserId(userId);
            message.setType(2);
            messageMapper.insert(message);
        }

        // Also increment room like count periodically
        if (totalLikes % 100 == 0) {
            liveRoomMapper.incrementLikeCount(roomId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("type", "like");
        response.put("userId", userId);
        response.put("totalLikes", totalLikes);
        return response;
    }

    private Map<String, Object> handleGift(Long roomId, WebSocketSession session, Map<String, Object> msg) {
        Long userId = parseUserId(msg);
        Number giftId = (Number) msg.get("giftId");
        Number count = (Number) msg.get("count");

        if (giftId == null || count == null) return null;

        try {
            // 调用 GiftService 处理送礼（扣减余额、记录消息）
            Long messageId = giftService.sendGift(userId, giftId.longValue(), count.intValue(), roomId);

            // 获取礼物信息用于广播
            Gift gift = giftService.getById(giftId.longValue());
            String giftName = gift != null ? gift.getName() : "礼物";
            String giftIcon = gift != null ? gift.getIcon() : "";
            String giftAnimation = gift != null ? gift.getAnimationUrl() : "";

            Map<String, Object> response = new HashMap<>();
            response.put("type", "gift");
            response.put("userId", userId);
            response.put("nickname", msg.getOrDefault("nickname", "用户"));
            response.put("giftId", giftId);
            response.put("giftName", giftName);
            response.put("giftIcon", giftIcon);
            response.put("giftAnimation", giftAnimation);
            response.put("count", count);
            return response;
        } catch (BusinessException e) {
            // 余额不足等业务异常，返回错误信息给用户
            Map<String, Object> error = new HashMap<>();
            error.put("type", "error");
            error.put("message", e.getMessage());
            return error;
        }
    }

    private Long parseUserId(Map<String, Object> msg) {
        Object userId = msg.get("userId");
        if (userId instanceof Number) return ((Number) userId).longValue();
        if (userId instanceof String) return Long.parseLong((String) userId);
        return 0L;
    }

    private String filterSensitiveWords(String content) {
        return content.replaceAll("(?i)(fuck|shit|damn)", "***");
    }
}
