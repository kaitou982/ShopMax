package com.shop.common.feign.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单简要信息（Feign 共享 DTO）
 *
 * @author shop
 * @since 2026-06-17
 */
@Data
public class OrderSimpleResponse {

    private String orderNo;

    private Integer status;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal freightAmount;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime deliveryTime;
}
