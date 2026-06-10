package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建促销活动请求")
public class PromotionCreateRequest {

    @NotBlank(message = "活动名称不能为空")
    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "活动描述")
    private String description;

    @NotNull(message = "促销类型不能为空")
    @Schema(description = "类型: 1-满减 2-满折")
    private Integer type;

    @NotNull(message = "门槛金额不能为空")
    @Schema(description = "门槛金额")
    private BigDecimal minAmount;

    @Schema(description = "减免金额(满减)")
    private BigDecimal discountAmount;

    @Schema(description = "折扣率(满折)")
    private BigDecimal discountRate;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "适用类型: 1-全部 2-指定分类 3-指定商品")
    private Integer applicableType;

    @Schema(description = "适用分类/商品ID列表(JSON)")
    private String applicableIds;
}
