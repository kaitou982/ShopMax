package com.shop.live.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "直播商品响应")
public class LiveProductResponse {

    private Long id;
    private Long roomId;
    private Long productId;
    private Long skuId;
    private BigDecimal livePrice;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
}
