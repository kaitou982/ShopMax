package com.shop.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流轨迹实体
 *
 * @author shop
 * @since 2026-06-07
 */
@Data
@TableName("oms_logistics_trace")
public class LogisticsTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 物流ID */
    private Long logisticsId;

    /** 轨迹时间 */
    private LocalDateTime traceTime;

    /** 轨迹内容 */
    private String content;

    /** 当前位置 */
    private String location;

    /** 地点编码 */
    private String locationCode;

    /** 纬度 */
    private BigDecimal latitude;

    /** 经度 */
    private BigDecimal longitude;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
