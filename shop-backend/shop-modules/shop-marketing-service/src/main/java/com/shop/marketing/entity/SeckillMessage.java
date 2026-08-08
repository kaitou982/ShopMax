package com.shop.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀本地消息实体
 *
 * @author shop
 * @since 2026-06-15
 */
@Data
@TableName("mms_seckill_message")
public class SeckillMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息类型：SECKILL_ORDER
     */
    private String messageType;

    /**
     * 业务ID：秒杀订单号
     */
    private String businessId;

    /**
     * 消息内容(JSON)
     */
    private String content;

    /**
     * 状态：0-待处理 1-已处理 2-处理失败 3-已死信
     */
    private Integer status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
