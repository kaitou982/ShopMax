package com.shop.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流信息实体
 *
 * @author shop
 * @since 2026-06-07
 */
@Data
@TableName("oms_logistics")
public class Logistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 物流单号 */
    private String logisticsNo;

    /** 物流公司 */
    private String company;

    /** 状态: 0-已发货 1-运输中 2-派送中 3-已签收 */
    private Integer status;

    /** 发件人姓名 */
    private String senderName;

    /** 发件人电话 */
    private String senderPhone;

    /** 发件人地址 */
    private String senderAddress;

    /** 发件人纬度 */
    private BigDecimal senderLatitude;

    /** 发件人经度 */
    private BigDecimal senderLongitude;

    /** 收件人姓名 */
    private String receiverName;

    /** 收件人电话 */
    private String receiverPhone;

    /** 收件人地址 */
    private String receiverAddress;

    /** 收件人纬度 */
    private BigDecimal receiverLatitude;

    /** 收件人经度 */
    private BigDecimal receiverLongitude;

    /** 上次查询API时间 */
    private LocalDateTime lastQueryTime;

    @TableField(exist = false)
    private List<LogisticsTrace> traces;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
