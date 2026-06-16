package com.shop.marketing.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "秒杀商品响应")
public class SeckillProductResponse {

    private Long id;
    private Long sessionId;
    private Long productId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private Integer limitPerUser;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;

    // 商品信息字段（从商品表查询）
    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "商品原价")
    private BigDecimal originalPrice;

    @Schema(description = "已售数量")
    private Integer soldCount;
}
