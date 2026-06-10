package com.shop.common.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 *
 * @author shop
 * @since 2026-06-01
 */
@Getter
public enum PayMethod {

    ALIPAY(1, "支付宝"),
    WECHAT(2, "微信"),
    BALANCE(3, "余额");

    private final int code;
    private final String desc;

    PayMethod(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayMethod fromCode(Integer code) {
        if (code == null) return null;
        for (PayMethod m : values()) {
            if (m.code == code) return m;
        }
        return null;
    }
}
