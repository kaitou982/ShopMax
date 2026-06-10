package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建优惠券请求")
public class CouponCreateRequest {

    @NotBlank(message = "优惠券名称不能为空")
    @Schema(description = "优惠券名称")
    private String name;

    @NotNull(message = "优惠券类型不能为空")
    @Schema(description = "类型: 1-满减券 2-折扣券 3-运费券 4-新人券")
    private Integer type;

    @Schema(description = "使用门槛金额")
    private BigDecimal minAmount;

    @Schema(description = "减免金额(满减券)")
    private BigDecimal discountAmount;

    @Schema(description = "折扣率(折扣券)")
    private BigDecimal discountRate;

    @NotNull(message = "发放总量不能为空")
    @Schema(description = "发放总量")
    private Integer totalCount;

    @Schema(description = "每人限领数量")
    private Integer perLimit;

    @Schema(description = "领取后有效天数")
    private Integer validDays;

    @Schema(description = "固定有效期-开始")
    private LocalDateTime useStartTime;

    @Schema(description = "固定有效期-结束")
    private LocalDateTime useEndTime;

    @Schema(description = "适用类型: 1-全部 2-指定分类 3-指定商品")
    private Integer applicableType;

    @Schema(description = "适用分类/商品ID列表(JSON)")
    private String applicableIds;

    @Schema(description = "积分兑换所需积分，0或null表示不支持积分兑换")
    private Integer integralCost;

    @Schema(description = "是否可叠加: 0-不可叠加 1-可叠加")
    private Integer stackable;

    @Schema(description = "使用说明")
    private String description;

    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer status;
}
