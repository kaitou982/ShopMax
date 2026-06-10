package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "更新促销活动请求")
public class PromotionUpdateRequest {

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "类型: 1-满减 2-满折")
    private Integer type;

    @Schema(description = "门槛金额")
    private BigDecimal minAmount;

    @Schema(description = "减免金额(满减)")
    private BigDecimal discountAmount;

    @Schema(description = "折扣率(满折)")
    private BigDecimal discountRate;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "适用类型: 1-全部 2-指定分类 3-指定商品")
    private Integer applicableType;

    @Schema(description = "适用分类/商品ID列表(JSON)")
    private String applicableIds;
}
