package com.shop.marketing.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "优惠券领取记录响应")
public class CouponReceiveResponse {

    private Long id;
    private Long couponId;
    private Long userId;
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private Long orderId;
    private String orderNo;
    private Integer status;

    /** 来自 Coupon 的冗余字段 */
    private String couponName;
    private Integer couponType;
    private BigDecimal minAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private LocalDateTime useEndTime;
    private Integer applicableType;
    private String applicableIds;

    private LocalDateTime createTime;
}
