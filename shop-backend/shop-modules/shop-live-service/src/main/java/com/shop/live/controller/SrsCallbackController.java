package com.shop.live.controller;

import com.shop.live.service.SrsCallbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SRS 流媒体服务器回调控制器
 * 处理推流/断流/播放鉴权等回调
 *
 * SRS 回调格式：JSON POST
 */
@Slf4j
@Tag(name = "SRS回调")
@RestController
@RequestMapping("/api/v1/live/srs")
@RequiredArgsConstructor
public class SrsCallbackController {

    private final SrsCallbackService srsCallbackService;

    /**
     * SRS 推流/断流回调
     */
    @Operation(summary = "推流/断流回调")
    @PostMapping("/callback")
    public String callback(@RequestBody Map<String, Object> body) {
        String app = (String) body.get("app");
        String stream = (String) body.get("stream");
        String action = (String) body.get("action");
        String param = (String) body.get("param");

        log.info("SRS回调: app={}, stream={}, action={}, param={}", app, stream, action, param);
        return srsCallbackService.handleCallback(app, stream, action, param);
    }

    /**
     * SRS 播放鉴权回调
     * 返回 "0" 允许播放，非 "0" 拒绝
     */
    @Operation(summary = "播放鉴权回调")
    @PostMapping("/on-play")
    public String onPlay(@RequestBody Map<String, Object> body) {
        String stream = (String) body.get("stream");
        String param = (String) body.get("param");

        log.info("SRS播放鉴权: stream={}, param={}", stream, param);
        return srsCallbackService.verifyPlayAccess(stream, param);
    }
}
