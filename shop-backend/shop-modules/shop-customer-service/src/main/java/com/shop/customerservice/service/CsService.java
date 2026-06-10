package com.shop.customerservice.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.exception.BusinessException;
import com.shop.common.redis.RedisUtil;
import com.shop.common.web.PageResult;
import com.shop.customerservice.dto.ChatRequest;
import com.shop.customerservice.dto.ChatResponse;
import com.shop.customerservice.entity.CsFaq;
import com.shop.customerservice.entity.CsMessage;
import com.shop.customerservice.entity.CsSession;
import com.shop.customerservice.handler.CsWebSocketHandler;
import com.shop.customerservice.mapper.CsFaqMapper;
import com.shop.customerservice.mapper.CsMessageMapper;
import com.shop.customerservice.mapper.CsSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsService {

    private static final int MAX_ACTIVE_SESSIONS = 10;
    private static final int MAX_MESSAGES_PER_SESSION = 200;
    private static final int MAX_HISTORY_CONTEXT = 20;
    private static final int RATE_LIMIT_MESSAGES = 10;
    private static final int RATE_LIMIT_WINDOW_SECONDS = 60;

    private final CsSessionMapper sessionMapper;
    private final CsMessageMapper messageMapper;
    private final CsFaqMapper faqMapper;
    private final AiService aiService;
    private final CsWebSocketHandler webSocketHandler;
    private final RedisUtil redisUtil;

    @Transactional(rollbackFor = Exception.class)
    public ChatResponse sendMessage(String sessionNo, ChatRequest request, Long userId, String username) {
        // Rate limit check
        checkRateLimit(userId);

        // Get session with auth
        CsSession session = getSessionWithAuth(sessionNo, userId);

        // Check message count limit
        long messageCount = messageMapper.countBySessionId(session.getId());
        if (messageCount >= MAX_MESSAGES_PER_SESSION) {
            throw new BusinessException("会话消息过多，请创建新会话");
        }

        // Filter content
        String content = filterContent(request.getContent());

        // Save user message
        CsMessage userMsg = new CsMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(content);
        messageMapper.insert(userMsg);

        // Update session last message time
        session.setLastMessageTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        // Get recent history
        List<CsMessage> history = messageMapper.selectRecentBySessionId(session.getId(), MAX_HISTORY_CONTEXT);
        // Reverse to chronological order for AI
        List<CsMessage> chronologicalHistory = new ArrayList<>(history);
        chronologicalHistory = chronologicalHistory.reversed();

        // Remove the user message we just added from the end (it will be added back by buildMessages)
        // Actually, keep it - buildMessages will use the last 20 including this one

        // Call AI
        AiService.AiResult aiResult;
        try {
            aiResult = aiService.chat(sessionNo, chronologicalHistory, userId, username);
        } catch (AiServiceException e) {
            log.error("AI 调用失败: sessionNo={}, error={}", sessionNo, e.getMessage(), e);
            // Save error message
            CsMessage errMsg = new CsMessage();
            errMsg.setSessionId(session.getId());
            errMsg.setRole("assistant");
            errMsg.setContent("抱歉，AI 服务暂时不可用，请稍后再试或输入\"人工客服\"寻求帮助。");
            errMsg.setTokenCount(0);
            messageMapper.insert(errMsg);
            return convertToResponse(errMsg);
        }

        // Save assistant message
        CsMessage aiMsg = new CsMessage();
        aiMsg.setSessionId(session.getId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(aiResult.content());
        aiMsg.setTokenCount(aiResult.tokenCount());
        messageMapper.insert(aiMsg);

        session.setLastMessageTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        // Push via WebSocket
        ChatResponse response = convertToResponse(aiMsg);
        webSocketHandler.pushMessage(sessionNo, response);

        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public CsSession createSession(Long userId) {
        // Check active session limit
        long activeCount = sessionMapper.countActiveByUserId(userId);
        if (activeCount >= MAX_ACTIVE_SESSIONS) {
            throw new BusinessException("进行中的会话已达上限，请先结束已有会话");
        }

        // Rate limit check for session creation
        String key = "shop:cs:rate:" + userId + ":create_session";
        Long count = redisUtil.increment(key, 1);
        if (count == 1) {
            redisUtil.expire(key, 3600, TimeUnit.SECONDS);
        }
        if (count > 5) {
            throw new BusinessException("创建会话过于频繁，请稍后再试");
        }

        CsSession session = new CsSession();
        session.setSessionNo(generateSessionNo());
        session.setUserId(userId);
        session.setStatus(0);
        session.setLastMessageTime(LocalDateTime.now());
        sessionMapper.insert(session);

        log.info("创建会话: sessionNo={}, userId={}", session.getSessionNo(), userId);
        return session;
    }

    @Transactional(rollbackFor = Exception.class)
    public void closeSession(String sessionNo, Long userId) {
        CsSession session = getSessionWithAuth(sessionNo, userId);
        session.setStatus(1);
        sessionMapper.updateById(session);
        log.info("关闭会话: sessionNo={}, userId={}", sessionNo, userId);
    }

    public List<CsSession> getMySessions(Long userId) {
        return sessionMapper.selectActiveByUserId(userId);
    }

    public PageResult<CsMessage> getMessages(String sessionNo, Long userId, Integer pageNum, Integer pageSize) {
        CsSession session = getSessionWithAuth(sessionNo, userId);

        Page<CsMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CsMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CsMessage::getSessionId, session.getId())
               .eq(CsMessage::getDeleted, 0)
               .orderByAsc(CsMessage::getCreateTime);

        Page<CsMessage> result = messageMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    // ========== FAQ Management ==========

    public PageResult<CsFaq> getFaqPage(Integer pageNum, Integer pageSize, String category) {
        Page<CsFaq> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CsFaq> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CsFaq::getDeleted, 0);
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(CsFaq::getCategory, category);
        }
        wrapper.orderByAsc(CsFaq::getCategory).orderByAsc(CsFaq::getSortOrder);

        Page<CsFaq> result = faqMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    public CsFaq createFaq(CsFaq faq) {
        faqMapper.insert(faq);
        log.info("新增FAQ: id={}, question={}", faq.getId(), faq.getQuestion());
        return faq;
    }

    public CsFaq updateFaq(Long id, CsFaq faq) {
        CsFaq existing = faqMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("FAQ不存在");
        }
        faq.setId(id);
        faqMapper.updateById(faq);
        log.info("更新FAQ: id={}", id);
        return faq;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFaq(Long id) {
        CsFaq faq = faqMapper.selectById(id);
        if (faq == null) {
            throw new BusinessException("FAQ不存在");
        }
        faqMapper.deleteById(id);
        log.info("删除FAQ: id={}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchImportFaq(List<CsFaq> items) {
        int count = 0;
        for (CsFaq item : items) {
            if (StrUtil.isBlank(item.getQuestion()) || StrUtil.isBlank(item.getAnswer())) {
                continue;
            }
            if (item.getSortOrder() == null) {
                item.setSortOrder(0);
            }
            if (item.getStatus() == null) {
                item.setStatus(1);
            }
            faqMapper.insert(item);
            count++;
        }
        log.info("批量导入FAQ: count={}", count);
        return count;
    }

    public List<CsFaq> exportFaq() {
        LambdaQueryWrapper<CsFaq> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CsFaq::getDeleted, 0)
               .orderByAsc(CsFaq::getCategory)
               .orderByAsc(CsFaq::getSortOrder);
        return faqMapper.selectList(wrapper);
    }

    // ========== Admin Session View ==========

    public PageResult<CsSession> getAdminSessions(Integer pageNum, Integer pageSize, Long userId) {
        Page<CsSession> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CsSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CsSession::getDeleted, 0);
        if (userId != null) {
            wrapper.eq(CsSession::getUserId, userId);
        }
        wrapper.orderByDesc(CsSession::getCreateTime);

        Page<CsSession> result = sessionMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getPages());
    }

    // ========== Private Helpers ==========

    private CsSession getSessionWithAuth(String sessionNo, Long userId) {
        CsSession session = sessionMapper.selectBySessionNo(sessionNo);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此会话");
        }
        return session;
    }

    private String generateSessionNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = RandomUtil.randomString(6).toUpperCase();
        return "CS-" + date + "-" + random;
    }

    private String filterContent(String content) {
        // Strip HTML tags
        return content.replaceAll("<[^>]*>", "")
                .replaceAll("(?i)<script.*?>.*?</script>", "")
                .trim();
    }

    private void checkRateLimit(Long userId) {
        String key = "shop:cs:rate:" + userId + ":message";
        Long count = redisUtil.increment(key, 1);
        if (count == 1) {
            redisUtil.expire(key, RATE_LIMIT_WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (count > RATE_LIMIT_MESSAGES) {
            throw new BusinessException("发送太快了，请稍后再试");
        }
    }

    private ChatResponse convertToResponse(CsMessage msg) {
        return ChatResponse.builder()
                .messageId(msg.getId())
                .role(msg.getRole())
                .content(msg.getContent())
                .tokenCount(msg.getTokenCount())
                .createTime(msg.getCreateTime())
                .build();
    }
}
