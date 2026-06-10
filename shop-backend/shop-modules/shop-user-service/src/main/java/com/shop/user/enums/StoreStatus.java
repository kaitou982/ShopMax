package com.shop.user.enums;

import lombok.Getter;

/**
 * 店家审核状态枚举
 *
 * @author shop
 * @since 2026-05-21
 */
@Getter
public enum StoreStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;

    StoreStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StoreStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StoreStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }
}
