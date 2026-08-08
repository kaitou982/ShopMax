package com.shop.admin.service;

import com.shop.common.feign.client.InternalOrderClient;
import com.shop.common.feign.client.InternalUserClient;
import com.shop.common.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InternalOrderClient internalOrderClient;
    private final InternalUserClient internalUserClient;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // 从订单服务获取统计数据
        Result<Map<String, Object>> orderStatsResult = internalOrderClient.getDashboardStats();
        Map<String, Object> orderStats = orderStatsResult.getData();

        long todayOrders = getLong(orderStats, "today_orders");
        BigDecimal todaySales = getDecimal(orderStats, "today_sales");
        long pendingOrders = getLong(orderStats, "pending_orders");
        long yesterdayOrders = getLong(orderStats, "yesterday_orders");
        BigDecimal yesterdaySales = getDecimal(orderStats, "yesterday_sales");

        // 从用户服务获取注册统计
        Result<Map<String, Object>> userStatsResult = internalUserClient.getRegisterStats();
        Map<String, Object> userStats = userStatsResult.getData();

        long todayNewUsers = getLong(userStats, "today_new_users");
        long yesterdayNewUsers = getLong(userStats, "yesterday_new_users");

        stats.put("todayOrders", todayOrders);
        stats.put("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);
        stats.put("todayNewUsers", todayNewUsers);
        stats.put("pendingOrders", pendingOrders);

        // 计算环比变化百分比
        stats.put("orderChange", calcChange(todayOrders, yesterdayOrders));
        stats.put("salesChange", calcChange(todaySales, yesterdaySales));
        stats.put("userChange", calcChange(todayNewUsers, yesterdayNewUsers));
        stats.put("pendingChange", calcChange(pendingOrders, 0)); // 昨日待处理数暂无

        return stats;
    }

    public List<Map<String, Object>> getSalesTrend() {
        Result<Map<String, Object>> result = internalOrderClient.getDashboardStats();
        Map<String, Object> orderStats = result.getData();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = (List<Map<String, Object>>) orderStats.get("sales_trend_7days");

        Map<String, BigDecimal> dateMap = new LinkedHashMap<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(fmt);
            dateMap.put(date, BigDecimal.ZERO);
        }

        if (raw != null) {
            for (Map<String, Object> row : raw) {
                Object dateObj = row.get("date");
                Object amountObj = row.get("amount");
                if (dateObj != null) {
                    String date = dateObj.toString();
                    BigDecimal amount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;
                    dateMap.put(date, amount);
                }
            }
        }

        List<Map<String, Object>> trendList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : dateMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", entry.getKey());
            item.put("amount", entry.getValue());
            trendList.add(item);
        }
        return trendList;
    }

    public List<Map<String, Object>> getRecentOrders() {
        // 最近订单需要通过 Feign 调用订单服务
        // 暂时返回空列表，后续可添加 /internal/orders/recent 端点
        return new ArrayList<>();
    }

    private long getLong(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) return 0L;
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        // 处理 String 类型（Jackson 将 Long 序列化为 String 的情况）
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private BigDecimal getDecimal(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) return BigDecimal.ZERO;
        return new BigDecimal(map.get(key).toString());
    }

    /** 计算环比变化百分比（保留一位小数），昨日为0则返回100表示无穷大 */
    private double calcChange(long today, long yesterday) {
        if (yesterday == 0) {
            return today > 0 ? 100.0 : 0.0;
        }
        return new BigDecimal((today - yesterday) * 100.0 / yesterday)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double calcChange(BigDecimal today, BigDecimal yesterday) {
        if (today == null) today = BigDecimal.ZERO;
        if (yesterday == null) yesterday = BigDecimal.ZERO;
        if (yesterday.compareTo(BigDecimal.ZERO) == 0) {
            return today.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return today.subtract(yesterday)
                .multiply(new BigDecimal("100"))
                .divide(yesterday, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
