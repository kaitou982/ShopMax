package com.shop.live.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "直播商品上架请求")
public class LiveProductAddRequest {

    @NotNull(message = "直播间ID不能为空")
    @Schema(description = "直播间ID")
    private Long roomId;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Long productId;

    @NotNull(message = "SKU ID不能为空")
    @Schema(description = "SKU ID")
    private Long skuId;

    @NotNull(message = "直播价格不能为空")
    @Schema(description = "直播价格")
    private BigDecimal livePrice;

    @Schema(description = "排序")
    private Integer sortOrder;
}
