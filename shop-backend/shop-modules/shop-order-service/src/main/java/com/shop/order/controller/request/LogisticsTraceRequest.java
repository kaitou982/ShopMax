package com.shop.order.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 添加物流轨迹请求
 *
 * @author shop
 * @since 2026-06-07
 */
@Data
@Schema(description = "添加物流轨迹请求")
public class LogisticsTraceRequest {

    @NotNull(message = "轨迹时间不能为空")
    @Schema(description = "轨迹时间")
    private LocalDateTime traceTime;

    @NotBlank(message = "轨迹内容不能为空")
    @Schema(description = "轨迹内容")
    private String content;

    @Schema(description = "当前位置")
    private String location;
}
