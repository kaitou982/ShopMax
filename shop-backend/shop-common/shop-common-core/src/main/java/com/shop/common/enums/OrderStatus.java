package com.shop.common.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author shop
 * @since 2026-06-01
 */
@Getter
public enum OrderStatus {

    PENDING_PAY(0, "待付款"),
    PENDING_SHIP(1, "待发货"),
    PENDING_RECEIVE(2, "待收货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消"),
    REFUNDING(5, "退款中"),
    REFUNDED(6, "已退款");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus fromCode(Integer code) {
        if (code == null) return null;
        for (OrderStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}
