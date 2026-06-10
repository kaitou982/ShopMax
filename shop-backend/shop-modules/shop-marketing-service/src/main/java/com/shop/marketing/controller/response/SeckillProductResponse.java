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
}
