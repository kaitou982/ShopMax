package com.shop.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("mms_group_buy_group")
public class GroupBuyGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    /** 团长用户ID */
    private Long leaderId;

    /** 当前参团人数 */
    private Integer currentCount;

    /** 成团所需人数 */
    private Integer requiredCount;

    /** 状态: 0-进行中 1-已成团 2-已失败 */
    private Integer status;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 成团/失败时间 */
    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
