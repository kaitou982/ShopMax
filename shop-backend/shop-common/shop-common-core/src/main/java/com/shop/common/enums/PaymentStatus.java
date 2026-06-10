package com.shop.common.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 *
 * @author shop
 * @since 2026-06-01
 */
@Getter
public enum PaymentStatus {

    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败"),
    REFUNDING(3, "退款中"),
    REFUNDED(4, "已退款");

    private final int code;
    private final String desc;

    PaymentStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaymentStatus fromCode(Integer code) {
        if (code == null) return null;
        for (PaymentStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}
