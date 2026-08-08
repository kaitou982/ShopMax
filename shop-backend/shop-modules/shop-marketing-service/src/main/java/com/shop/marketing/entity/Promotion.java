package com.shop.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mms_promotion")
public class Promotion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String description;

    /** 类型: 1-满减 2-满折 */
    private Integer type;

    /** 门槛金额 */
    private BigDecimal minAmount;

    /** 减免金额(满减) */
    private BigDecimal discountAmount;

    /** 折扣率(满折) */
    private BigDecimal discountRate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 适用类型: 1-全部 2-指定分类 3-指定商品 */
    private Integer applicableType;

    /** 适用分类/商品ID列表(JSON) */
    private String applicableIds;

    /** 状态: 0-未开始 1-进行中 2-已结束 3-已禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
