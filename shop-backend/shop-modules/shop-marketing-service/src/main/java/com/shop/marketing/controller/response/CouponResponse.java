package com.shop.marketing.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "优惠券响应")
public class CouponResponse {

    private Long id;
    private String name;
    private Integer type;
    private BigDecimal minAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private Integer totalCount;
    private Integer receivedCount;
    private Integer usedCount;
    private Integer perLimit;
    private Integer validDays;
    private LocalDateTime useStartTime;
    private LocalDateTime useEndTime;
    private Integer applicableType;
    private String applicableIds;
    private Integer integralCost;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
