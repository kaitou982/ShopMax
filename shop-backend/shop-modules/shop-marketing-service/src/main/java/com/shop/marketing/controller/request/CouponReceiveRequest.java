package com.shop.marketing.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "领取优惠券请求")
public class CouponReceiveRequest {

    @NotNull(message = "优惠券ID不能为空")
    @Schema(description = "优惠券ID")
    private Long couponId;
}
