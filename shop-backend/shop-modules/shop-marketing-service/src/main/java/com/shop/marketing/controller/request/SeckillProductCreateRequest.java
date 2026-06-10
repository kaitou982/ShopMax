package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "添加秒杀商品请求")
public class SeckillProductCreateRequest {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Long productId;

    @NotNull(message = "SKU ID不能为空")
    @Schema(description = "SKU ID")
    private Long skuId;

    @NotNull(message = "秒杀价不能为空")
    @Schema(description = "秒杀价")
    private BigDecimal seckillPrice;

    @NotNull(message = "秒杀库存不能为空")
    @Schema(description = "秒杀库存")
    private Integer seckillStock;

    @Schema(description = "每人限购")
    private Integer limitPerUser;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer status;
}
