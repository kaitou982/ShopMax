package com.shop.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物流公司枚举
 *
 * @author shop
 * @since 2026-06-07
 */
@Getter
@AllArgsConstructor
public enum LogisticsCompany {

    SF("顺丰速运", "shunfeng"),
    YD("韵达快递", "yunda"),
    YT("圆通速递", "yuantong"),
    ZT("中通快递", "zhongtong"),
    ST("申通快递", "shentong"),
    JD("京东物流", "jd"),
    EMS("EMS", "ems"),
    DB("德邦快递", "debang"),
    JT("极兔速递", "jtexpress"),
    HT("百世快递", "huitongkuaidi");

    /** 公司名称 */
    private final String name;

    /** 快递100编码 */
    private final String code;

    /**
     * 根据编码查找物流公司
     */
    public static LogisticsCompany fromCode(String code) {
        for (LogisticsCompany company : values()) {
            if (company.getCode().equals(code)) {
                return company;
            }
        }
        return null;
    }

    /**
     * 根据名称查找物流公司
     */
    public static LogisticsCompany fromName(String name) {
        for (LogisticsCompany company : values()) {
            if (company.getName().equals(name)) {
                return company;
            }
        }
        return null;
    }
}
