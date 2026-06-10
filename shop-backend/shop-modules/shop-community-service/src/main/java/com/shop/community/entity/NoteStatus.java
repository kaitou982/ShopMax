package com.shop.community.entity;

import lombok.Getter;

@Getter
public enum NoteStatus {
    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布"),
    UNDER_REVIEW(3, "审核中"),
    REJECTED(4, "已驳回");

    private final int code;
    private final String desc;

    NoteStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(int code) {
        for (var status : values()) {
            if (status.code == code) {
                return true;
            }
        }
        return false;
    }
}
