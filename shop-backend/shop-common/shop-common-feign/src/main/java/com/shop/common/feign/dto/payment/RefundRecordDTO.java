package com.shop.common.feign.dto.payment;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录DTO（用于 Feign 传输，避免 admin-service 直接依赖 payment-service 的 Entity）
 */
@Data
public class RefundRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String refundNo;
    private Long userId;
    private String orderNo;
    private String paymentNo;
    private BigDecimal refundAmount;
    private Integer payMethod;
    private Integer status;
    private String refundReason;
    private String failReason;
    private String gatewayRefundNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
