package com.shop.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mms_coupon")
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    /** 类型: 1-满减券 2-折扣券 3-运费券 4-新人券 */
    private Integer type;

    /** 使用门槛金额 */
    private BigDecimal minAmount;

    /** 减免金额(满减券) */
    private BigDecimal discountAmount;

    /** 折扣率(折扣券) */
    private BigDecimal discountRate;

    /** 发放总量 */
    private Integer totalCount;

    /** 已领取数量 */
    private Integer receivedCount;

    /** 已使用数量 */
    private Integer usedCount;

    /** 每人限领数量 */
    private Integer perLimit;

    /** 领取后有效天数 */
    private Integer validDays;

    /** 固定有效期-开始 */
    private LocalDateTime useStartTime;

    /** 固定有效期-结束 */
    private LocalDateTime useEndTime;

    /** 适用类型: 1-全部 2-指定分类 3-指定商品 */
    private Integer applicableType;

    /** 适用分类/商品ID列表(JSON) */
    private String applicableIds;

    /** 积分兑换所需积分 */
    private Integer integralCost;

    /** 是否可叠加: 0-不可叠加 1-可叠加 */
    private Integer stackable;

    private String description;

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
