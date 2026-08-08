package com.shop.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单主表实体
 *
 * @author shop
 * @since 2026-04-22
 */
@Data
@TableName("oms_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 应付金额
     */
    private BigDecimal payAmount;

    /**
     * 运费
     */
    private BigDecimal freightAmount;

    /**
     * 优惠券抵扣金额
     */
    private BigDecimal couponAmount;

    /**
     * 积分抵扣金额
     */
    private BigDecimal integralAmount;

    /**
     * 使用的积分数（前端传入，非DB字段）
     */
    @TableField(exist = false)
    private Integer useIntegral;

    /**
     * 订单商品列表（前端传入，非DB字段）
     */
    @TableField(exist = false)
    private List<OrderItem> items;

    /**
     * 使用的优惠券领取记录ID
     */
    private Long userCouponId;

    /**
     * 叠加的第二张优惠券领取记录ID
     */
    private Long userCouponId2;

    /**
     * 订单状态: 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-退款中 6-已退款
     */
    private Integer status;

    /**
     * 支付方式: 1-支付宝 2-微信 3-余额
     */
    private Integer payType;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 买家留言
     */
    private String remark;

    /**
     * 订单来源: 1-PC 2-H5 3-小程序 4-APP
     */
    private Integer sourceType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
