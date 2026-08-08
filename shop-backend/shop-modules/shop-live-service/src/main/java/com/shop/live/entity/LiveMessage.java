package com.shop.live.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("lms_live_message")
public class LiveMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roomId;

    private Long userId;

    /** 类型: 1-弹幕 2-点赞 3-礼物 4-进入 5-关注 */
    private Integer type;

    private String content;

    private Long giftId;

    private Integer giftCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
