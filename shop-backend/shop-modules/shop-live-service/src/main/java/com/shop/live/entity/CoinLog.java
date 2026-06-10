package com.shop.live.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 虚拟币流水实体
 */
@Data
@TableName("lms_coin_log")
public class CoinLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 变动数量(正增负减) */
    private Integer amount;

    /** 类型: 1-注册赠送 2-每日签到 3-送礼消费 4-系统赠送 */
    private Integer type;

    /** 关联业务ID(消息ID) */
    private String bizId;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
