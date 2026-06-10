package com.shop.customerservice.controller;

import com.shop.common.web.Result;
import com.shop.common.web.PageResult;
import com.shop.customerservice.dto.ChatRequest;
import com.shop.customerservice.dto.ChatResponse;
import com.shop.customerservice.entity.CsMessage;
import com.shop.customerservice.entity.CsSession;
import com.shop.customerservice.service.CsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "智能客服")
@RestController
@RequestMapping("/api/v1/cs")
@RequiredArgsConstructor
public class CsController {

    private final CsService csService;

    @Operation(summary = "创建客服会话")
    @PostMapping("/sessions")
    public Result<CsSession> createSession(@RequestAttribute("userId") Long userId) {
        return Result.success(csService.createSession(userId));
    }

    @Operation(summary = "获取我的会话列表")
    @GetMapping("/sessions/my")
    public Result<List<CsSession>> getMySessions(@RequestAttribute("userId") Long userId) {
        return Result.success(csService.getMySessions(userId));
    }

    @Operation(summary = "关闭会话")
    @PostMapping("/sessions/{sessionNo}/close")
    public Result<Void> closeSession(@PathVariable String sessionNo,
                                      @RequestAttribute("userId") Long userId) {
        csService.closeSession(sessionNo, userId);
        return Result.success();
    }

    @Operation(summary = "获取会话历史消息")
    @GetMapping("/sessions/{sessionNo}/messages")
    public Result<PageResult<CsMessage>> getMessages(@PathVariable String sessionNo,
                                                      @RequestAttribute("userId") Long userId,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.success(csService.getMessages(sessionNo, userId, pageNum, pageSize));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/sessions/{sessionNo}/messages")
    public Result<ChatResponse> sendMessage(@PathVariable String sessionNo,
                                             @RequestAttribute("userId") Long userId,
                                             @RequestAttribute(value = "username", required = false) String username,
                                             @Valid @RequestBody ChatRequest request) {
        if (username == null) {
            username = "用户" + userId;
        }
        return Result.success(csService.sendMessage(sessionNo, request, userId, username));
    }
}
