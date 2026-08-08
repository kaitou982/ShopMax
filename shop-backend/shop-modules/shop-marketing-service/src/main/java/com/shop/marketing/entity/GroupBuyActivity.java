package com.shop.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mms_group_buy_activity")
public class GroupBuyActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private Long productId;

    private Long skuId;

    /** 拼团价格 */
    private BigDecimal groupPrice;

    /** 成团人数 */
    private Integer requiredCount;

    /** 拼团有效小时数 */
    private Integer expireHours;

    /** 拼团库存 */
    private Integer stock;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
