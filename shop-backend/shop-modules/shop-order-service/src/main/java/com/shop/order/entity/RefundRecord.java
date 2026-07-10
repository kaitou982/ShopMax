package com.shop.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体
 *
 * @author shop
 * @since 2026-06-23
 */
@Data
@TableName("oms_refund_record")
public class RefundRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 退款单号 */
    private String refundNo;

    /** 关联支付单号 */
    private String paymentNo;

    /** 关联订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 本次退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /** 退款状态: 0-处理中 1-成功 2-失败 */
    private Integer status;

    /** 支付方式: 1-支付宝 2-微信 3-余额 */
    private Integer payMethod;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
