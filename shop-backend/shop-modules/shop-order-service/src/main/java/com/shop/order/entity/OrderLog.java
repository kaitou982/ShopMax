package com.shop.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单操作日志实体
 *
 * @author shop
 * @since 2026-06-02
 */
@Data
@TableName("oms_order_log")
public class OrderLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 操作前状态描述 */
    private String oldStatus;

    /** 操作动作: CREATE/PAY/SHIP/CONFIRM/CANCEL/REFUND/REFUND_APPLY/DELETE */
    private String action;

    /** 备注 */
    private String remark;

    /** 操作人ID（系统操作时为 null） */
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
