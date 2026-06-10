package com.shop.live.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.live.service.LiveInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveWebSocketHandler extends TextWebSocketHandler {

    private final LiveMessageHandler liveMessageHandler;
    private final LiveInteractionService interactionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** roomId -> Map<sessionId, WebSocketSession> */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long roomId = extractRoomId(session);
        if (roomId == null) {
            try { session.close(); } catch (Exception ignored) {}
            return;
        }

        roomSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);

        session.getAttributes().put("roomId", roomId);

        int onlineCount = roomSessions.get(roomId).size();
        interactionService.incrementOnlineCount(roomId);

        // Broadcast user enter event
        Map<String, Object> onlineMap = new HashMap<>();
        onlineMap.put("type", "online");
        onlineMap.put("count", onlineCount);

        broadcast(roomId, onlineMap);

        log.info("WebSocket连接建立: roomId={}, sessionId={}, online={}", roomId, session.getId(), onlineCount);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long roomId = extractRoomId(session);
        if (roomId == null) return;

        try {
            Map<String, Object> result = liveMessageHandler.handleMessage(roomId, session, message.getPayload());
            if (result != null) {
                broadcast(roomId, result);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long roomId = extractRoomId(session);
        if (roomId == null) return;

        ConcurrentHashMap<String, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }

        interactionService.decrementOnlineCount(roomId);
        int onlineCount = sessions != null ? sessions.size() : 0;

        // Broadcast user leave event
        Map<String, Object> data = new HashMap<>();
        data.put("type", "online");
        data.put("count", onlineCount);
        broadcast(roomId, data);

        log.info("WebSocket连接关闭: roomId={}, sessionId={}, online={}", roomId, session.getId(), onlineCount);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误: sessionId={}, error={}", session.getId(), exception.getMessage());
    }

    public void broadcast(Long roomId, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            sendToRoom(roomId, json);
        } catch (Exception e) {
            log.error("序列化消息失败: {}", e.getMessage());
        }
    }

    public void sendToRoom(Long roomId, String message) {
        ConcurrentHashMap<String, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) return;

        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (Exception e) {
                    log.error("发送消息失败: sessionId={}", session.getId());
                }
            }
        }
    }

    public void sendToSession(WebSocketSession session, String message) {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("发送消息失败: sessionId={}", session.getId());
            }
        }
    }

    private Long extractRoomId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) return null;
            String path = uri.getPath();
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                return Long.parseLong(parts[3]);
            }
        } catch (Exception e) {
            log.error("解析roomId失败: {}", e.getMessage());
        }
        return null;
    }
}
