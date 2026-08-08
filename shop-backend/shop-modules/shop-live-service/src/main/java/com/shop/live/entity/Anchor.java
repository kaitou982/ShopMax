package com.shop.live.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("lms_anchor")
public class Anchor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String realName;

    private String phone;

    private String nickname;

    private String avatar;

    private String cover;

    private String introduction;

    /** 状态: 0-待审核 1-已通过 2-已拒绝 3-已禁用 */
    private Integer status;

    private String rejectReason;

    /** 主播等级: 1-普通 2-铜牌 3-银牌 4-金牌 5-钻石 */
    private Integer level;

    private Integer fansCount;

    private Integer totalLiveCount;

    /** 累计直播时长(秒) */
    private Long totalDuration;

    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
