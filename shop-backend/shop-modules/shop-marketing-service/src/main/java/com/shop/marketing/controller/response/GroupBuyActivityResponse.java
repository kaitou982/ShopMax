package com.shop.marketing.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "拼团活动响应")
public class GroupBuyActivityResponse {

    private Long id;
    private String name;
    private Long productId;
    private Long skuId;
    private BigDecimal groupPrice;
    private Integer requiredCount;
    private Integer expireHours;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createTime;
}
