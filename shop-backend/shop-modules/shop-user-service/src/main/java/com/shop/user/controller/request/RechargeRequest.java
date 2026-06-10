package com.shop.user.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "余额充值请求")
public class RechargeRequest {

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额最低0.01元")
    @Schema(description = "充值金额")
    private BigDecimal amount;

    @Schema(description = "支付渠道: alipay/wxpay")
    private String payChannel;
}
