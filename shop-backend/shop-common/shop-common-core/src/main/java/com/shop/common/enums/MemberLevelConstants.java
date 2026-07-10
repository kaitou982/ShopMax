package com.shop.common.enums;

/**
 * 会员等级常量（统一定义，避免多处重复）
 *
 * @author shop
 * @since 2026-06-17
 */
public final class MemberLevelConstants {

    private MemberLevelConstants() {}

    /** 等级名称（索引 0 未使用，1-4 对应四个等级） */
    public static final String[] LEVEL_NAMES = {"", "普通会员", "银卡会员", "金卡会员", "钻石会员"};

    /** 成长值阈值（索引 0-3 对应普通/银卡/金卡/钻石） */
    public static final int[] LEVEL_THRESHOLDS = {0, 500, 2000, 10000};

    /** 折扣系数（索引 0 未使用，1-4 对应四个等级） */
    public static final double[] LEVEL_DISCOUNTS = {1.0, 1.0, 0.98, 0.95, 0.90};

    /** 折扣描述（索引 0 未使用，1-4 对应四个等级） */
    public static final String[] LEVEL_DISCOUNT_LABELS = {"", "无折扣", "无折扣", "98折", "95折", "9折"};

    public static final int MAX_LEVEL = 4;

    /**
     * 获取等级名称，越界时返回"普通会员"
     */
    public static String getLevelName(int level) {
        if (level < 1 || level > MAX_LEVEL) return LEVEL_NAMES[1];
        return LEVEL_NAMES[level];
    }

    /**
     * 获取折扣系数，越界时返回 1.0（无折扣）
     */
    public static double getDiscount(int level) {
        if (level < 1 || level > MAX_LEVEL) return 1.0;
        return LEVEL_DISCOUNTS[level];
    }

    /**
     * 根据成长值计算等级
     */
    public static int calcLevel(int growthValue) {
        for (int i = MAX_LEVEL; i >= 1; i--) {
            if (growthValue >= LEVEL_THRESHOLDS[i - 1]) {
                return i;
            }
        }
        return 1;
    }

    /**
     * 获取下一等级所需成长值，已达最高级时返回最高级阈值
     */
    public static int getNextLevelGrowth(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) return LEVEL_THRESHOLDS[MAX_LEVEL - 1];
        return LEVEL_THRESHOLDS[currentLevel];
    }
}
