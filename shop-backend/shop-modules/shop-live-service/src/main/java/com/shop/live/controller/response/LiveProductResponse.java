package com.shop.live.controller.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "直播商品响应")
public class LiveProductResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long skuId;
    private BigDecimal livePrice;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
}
