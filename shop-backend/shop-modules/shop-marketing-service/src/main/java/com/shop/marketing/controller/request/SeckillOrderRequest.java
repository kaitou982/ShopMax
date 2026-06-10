package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "秒杀下单请求")
public class SeckillOrderRequest {

    @NotNull(message = "场次ID不能为空")
    @Schema(description = "场次ID")
    private Long sessionId;

    @NotNull(message = "秒杀商品ID不能为空")
    @Schema(description = "秒杀商品ID")
    private Long productId;
}
