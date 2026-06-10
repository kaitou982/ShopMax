package com.shop.customerservice.handler;

import cn.hutool.json.JSONUtil;
import com.shop.common.security.jwt.JwtUtil;
import com.shop.customerservice.entity.CsSession;
import com.shop.customerservice.mapper.CsSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final CsSessionMapper sessionMapper;

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionNo = extractSessionNo(session);
        if (sessionNo == null) {
            closeSession(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // Extract and validate token
        String token = extractToken(session);
        if (token == null || !jwtUtil.validateToken(token)) {
            log.warn("WebSocket 握手失败: token 无效, sessionNo={}", sessionNo);
            closeSession(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        // Verify session ownership
        CsSession csSession = sessionMapper.selectBySessionNo(sessionNo);
        if (csSession == null || !csSession.getUserId().equals(userId)) {
            log.warn("WebSocket 握手失败: 会话归属校验失败, sessionNo={}, userId={}", sessionNo, userId);
            closeSession(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        sessions.put(sessionNo, session);
        session.getAttributes().put("sessionNo", sessionNo);
        session.getAttributes().put("userId", userId);

        log.info("WebSocket 连接建立: sessionNo={}, userId={}", sessionNo, userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client-to-server messages are sent via HTTP POST, not WebSocket.
        // WebSocket is server-to-client only for AI reply push.
        // We still accept heartbeat pings from client.
        String payload = message.getPayload();
        if ("ping".equals(payload)) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (IOException e) {
                log.warn("心跳响应失败: sessionId={}", session.getId());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionNo = extractSessionNo(session);
        if (sessionNo != null) {
            sessions.remove(sessionNo);
            log.info("WebSocket 连接关闭: sessionNo={}, status={}", sessionNo, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionNo = extractSessionNo(session);
        log.error("WebSocket 传输错误: sessionNo={}, error={}", sessionNo, exception.getMessage());
    }

    /**
     * Push AI reply to user via WebSocket.
     */
    public void pushMessage(String sessionNo, Object data) {
        WebSocketSession session = sessions.get(sessionNo);
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket 推送失败，会话不在线: sessionNo={}", sessionNo);
            return;
        }
        try {
            String json = JSONUtil.toJsonStr(data);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("WebSocket 推送消息失败: sessionNo={}, error={}", sessionNo, e.getMessage());
        }
    }

    public boolean isOnline(String sessionNo) {
        WebSocketSession session = sessions.get(sessionNo);
        return session != null && session.isOpen();
    }

    private String extractSessionNo(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) return null;
            String path = uri.getPath();
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                return parts[3];
            }
        } catch (Exception e) {
            log.error("解析 sessionNo 失败: {}", e.getMessage());
        }
        return null;
    }

    private String extractToken(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) return null;
            String query = uri.getQuery();
            if (query == null) return null;

            String[] params = query.split("&");
            for (String param : params) {
                String[] kv = param.split("=", 2);
                if ("token".equals(kv[0]) && kv.length > 1) {
                    return kv[1];
                }
            }
        } catch (Exception e) {
            log.error("解析 token 失败: {}", e.getMessage());
        }
        return null;
    }

    private void closeSession(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException ignored) {
        }
    }
}
