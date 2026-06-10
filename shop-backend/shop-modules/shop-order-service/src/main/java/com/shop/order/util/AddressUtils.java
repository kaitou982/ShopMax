package com.shop.order.util;

import java.util.regex.Pattern;

/**
 * 地址工具类
 *
 * @author shop
 * @since 2026-06-08
 */
public final class AddressUtils {

    private AddressUtils() {}

    // ── 地址清洗：剔除导致地理编码偏差的细节信息 ──

    /** 门牌号后缀：7号、7号楼、7号院、7-1号 等 */
    private static final Pattern HOUSE_NUMBER_SUFFIX = Pattern.compile("[\\dA-Za-z]+[-]?[\\d]*号[楼栋院单元]?\\d*[室号]?$");
    /** 房间/楼层后缀：1001室、A座10层、20楼 等 */
    private static final Pattern ROOM_FLOOR_SUFFIX = Pattern.compile("[A-Za-z]?[\\d]*(室|层|楼|座|号房)$");
    /** 驿站/快递柜后缀 */
    private static final Pattern PICKUP_SUFFIX = Pattern.compile("(菜鸟驿站|丰巢|快递柜|代收点|驿站|自提柜|妈妈驿站)$");

    /**
     * 清洗地址文本，剔除末尾的门牌号、房间号、驿站等细节，
     * 提升高德地理编码 API 的匹配准确度。
     * <p>
     * 示例：
     * "广东省佛山市南海区狮山镇小塘长安路7号"       → "广东省佛山市南海区狮山镇小塘长安路"
     * "肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区学术交流中心" → "肇庆市鼎湖区莲花镇广州应用科技学院肇庆校区"
     * "北京市朝阳区建国路88号SOHO现代城A座1001室"    → "北京市朝阳区建国路88号SOHO现代城"
     * "深圳市南山区科技园南路1号腾讯大厦35层"        → "深圳市南山区科技园南路1号腾讯大厦"
     */
    public static String cleanForGeocoding(String address) {
        if (address == null || address.isBlank()) {
            return address;
        }

        String cleaned = address.trim();

        // 1. 剔除驿站/快递柜后缀
        cleaned = PICKUP_SUFFIX.matcher(cleaned).replaceAll("");

        // 2. 剔除房间/楼层后缀
        cleaned = ROOM_FLOOR_SUFFIX.matcher(cleaned).replaceAll("");

        // 3. 剔除门牌号后缀（仅当地址足够长时，避免过度清洗导致空地址）
        if (cleaned.length() > 12) {
            String afterHouseNum = HOUSE_NUMBER_SUFFIX.matcher(cleaned).replaceAll("");
            // 确保清洗后至少保留省市区级别的长度
            if (afterHouseNum.length() >= 8) {
                cleaned = afterHouseNum;
            }
        }

        cleaned = cleaned.trim();

        // 清洗后地址过短（<6字符），回退使用原始地址
        if (cleaned.length() < 6) {
            return address.trim();
        }

        return cleaned;
    }

    // ── 城市提取 ──

    /**
     * 从地址文本中提取城市名称
     * "广东省深圳市南山区..." → "深圳市"
     * "北京市朝阳区..."       → "北京市"
     * "新疆维吾尔自治区乌鲁木齐市..." → "乌鲁木齐市"
     */
    public static String extractCity(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }

        // 尝试匹配 "省xxx市" 或 "自治区xxx市"
        int provinceIdx = address.indexOf("省");
        if (provinceIdx < 0) {
            provinceIdx = address.indexOf("自治区");
        }
        if (provinceIdx < 0) {
            provinceIdx = address.indexOf("特别行政区");
        }

        int cityIdx = address.indexOf("市");
        if (provinceIdx >= 0 && cityIdx > provinceIdx) {
            return address.substring(provinceIdx + 1, cityIdx + 1);
        }

        // 直辖市等没有"省"的情况：取第一个"市"
        if (cityIdx >= 0) {
            int start = Math.max(0, cityIdx - 3);
            return address.substring(start, cityIdx + 1);
        }

        return "";
    }
}
