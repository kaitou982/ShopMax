package com.shop.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单实体
 *
 * @author shop
 * @since 2026-06-15
 */
@Data
@TableName("oms_seckill_order")
public class SeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 秒杀场次ID
     */
    private Long sessionId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀价
     */
    private BigDecimal seckillPrice;

    /**
     * 状态：0-待支付 1-已支付 2-已取消 3-已超时
     */
    private Integer status;

    /**
     * 支付过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 正式订单ID
     */
    private Long orderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
