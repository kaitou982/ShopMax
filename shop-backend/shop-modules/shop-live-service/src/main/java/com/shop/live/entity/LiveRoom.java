package com.shop.live.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("lms_live_room")
public class LiveRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long anchorId;

    private String title;

    private String cover;

    private String notice;

    /** 分类: 1-推荐 2-穿搭 3-美妆 4-美食 5-家居 6-数码 7-母婴 */
    private Integer type;

    private LocalDateTime startTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime endTime;

    private String pushUrl;

    private String pullUrl;

    /** 状态: 0-预告 1-直播中 2-已结束 3-已关闭 */
    private Integer status;

    private Integer onlineCount;

    private Integer totalViewCount;

    private Integer peakOnlineCount;

    private Integer likeCount;

    private Integer giftCount;

    /** 直播时长(秒) */
    private Long duration;

    /** 回放时长(秒) */
    private Integer replayDuration;

    private String replayUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
