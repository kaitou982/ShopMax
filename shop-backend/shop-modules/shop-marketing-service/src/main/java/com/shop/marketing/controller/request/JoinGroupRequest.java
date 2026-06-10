package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 参加拼团请求 — userId 由 JWT 解析，无需客户端传入
 */
@Data
@Schema(description = "参加拼团请求")
public class JoinGroupRequest {
}
