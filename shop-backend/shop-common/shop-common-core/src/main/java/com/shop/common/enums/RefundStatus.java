package com.shop.common.enums;

import lombok.Getter;

/**
 * 退款记录状态枚举
 *
 * @author shop
 * @since 2026-06-01
 */
@Getter
public enum RefundStatus {

    PROCESSING(0, "处理中"),
    SUCCESS(1, "退款成功"),
    FAILED(2, "退款失败");

    private final int code;
    private final String desc;

    RefundStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RefundStatus fromCode(Integer code) {
        if (code == null) return null;
        for (RefundStatus s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}
