package com.shop.order.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建物流请求
 *
 * @author shop
 * @since 2026-06-07
 */
@Data
@Schema(description = "创建物流请求")
public class LogisticsCreateRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Long orderId;

    @NotBlank(message = "物流单号不能为空")
    @Schema(description = "物流单号")
    private String logisticsNo;

    @NotBlank(message = "物流公司不能为空")
    @Schema(description = "物流公司")
    private String company;

    @Schema(description = "发件人姓名")
    private String senderName;

    @Schema(description = "发件人电话")
    private String senderPhone;

    @Schema(description = "发件人地址")
    private String senderAddress;

    @Schema(description = "收件人姓名")
    private String receiverName;

    @Schema(description = "收件人电话")
    private String receiverPhone;

    @Schema(description = "收件人地址")
    private String receiverAddress;

    @Schema(description = "发件人经度（选填，优先于自动地理编码）")
    private BigDecimal senderLongitude;

    @Schema(description = "发件人纬度（选填，优先于自动地理编码）")
    private BigDecimal senderLatitude;

    @Schema(description = "收件人经度（选填，优先于自动地理编码）")
    private BigDecimal receiverLongitude;

    @Schema(description = "收件人纬度（选填，优先于自动地理编码）")
    private BigDecimal receiverLatitude;
}
