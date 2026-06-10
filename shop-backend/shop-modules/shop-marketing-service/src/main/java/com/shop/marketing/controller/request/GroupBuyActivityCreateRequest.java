package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建拼团活动请求")
public class GroupBuyActivityCreateRequest {

    @NotBlank(message = "活动名称不能为空")
    @Schema(description = "活动名称")
    private String name;

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Long productId;

    @NotNull(message = "SKU ID不能为空")
    @Schema(description = "SKU ID")
    private Long skuId;

    @NotNull(message = "拼团价格不能为空")
    @Schema(description = "拼团价格")
    private BigDecimal groupPrice;

    @NotNull(message = "成团人数不能为空")
    @Schema(description = "成团人数")
    private Integer requiredCount;

    @Schema(description = "拼团有效小时数")
    private Integer expireHours;

    @Schema(description = "拼团库存")
    private Integer stock;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer status;
}
