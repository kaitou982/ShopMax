package com.shop.marketing.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "秒杀订单响应")
public class SeckillOrderResponse {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "秒杀场次ID")
    private Long sessionId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "秒杀价")
    private BigDecimal seckillPrice;

    @Schema(description = "状态：0-待支付 1-已支付 2-已取消 3-已超时")
    private Integer status;

    @Schema(description = "支付过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
