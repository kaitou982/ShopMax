package com.shop.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_payment")
public class Payment implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    /** 1-支付宝 2-微信 3-余额 */
    private Integer payMethod;
    /** 0-待支付 1-成功 2-失败 3-退款中 4-已退款 */
    private Integer status;
    private String transactionId;
    private LocalDateTime payTime;
    private LocalDateTime callbackTime;
    private LocalDateTime refundTime;
    private BigDecimal refundAmount;
    /** 退款原因（最近一次） */
    private String refundReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
