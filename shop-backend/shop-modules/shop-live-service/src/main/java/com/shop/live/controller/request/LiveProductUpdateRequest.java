package com.shop.live.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "直播商品更新请求")
public class LiveProductUpdateRequest {

    @Schema(description = "直播价格")
    private BigDecimal livePrice;

    @Schema(description = "排序")
    private Integer sortOrder;
}
