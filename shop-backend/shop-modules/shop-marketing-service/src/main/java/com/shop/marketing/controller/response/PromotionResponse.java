package com.shop.marketing.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "促销活动响应")
public class PromotionResponse {

    private Long id;
    private String name;
    private String description;
    private Integer type;
    private BigDecimal minAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer applicableType;
    private String applicableIds;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
