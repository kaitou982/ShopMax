package com.shop.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.admin.entity.Order;
import com.shop.admin.mapper.OrderMapper;
import com.shop.admin.mapper.UserMapper;
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

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // 今日数据
        long todayOrders = orderMapper.countTodayOrders();
        BigDecimal todaySales = orderMapper.sumTodaySales();
        long todayNewUsers = userMapper.countTodayNewUsers();
        long pendingOrders = orderMapper.countPendingOrders();

        stats.put("todayOrders", todayOrders);
        stats.put("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);
        stats.put("todayNewUsers", todayNewUsers);
        stats.put("pendingOrders", pendingOrders);

        // 昨日数据
        long yesterdayOrders = orderMapper.countYesterdayOrders();
        BigDecimal yesterdaySales = orderMapper.sumYesterdaySales();
        long yesterdayNewUsers = userMapper.countYesterdayNewUsers();
        long yesterdayPending = orderMapper.countYesterdayPendingOrders();

        // 计算环比变化百分比
        stats.put("orderChange", calcChange(todayOrders, yesterdayOrders));
        stats.put("salesChange", calcChange(todaySales, yesterdaySales));
        stats.put("userChange", calcChange(todayNewUsers, yesterdayNewUsers));
        stats.put("pendingChange", calcChange(pendingOrders, yesterdayPending));

        return stats;
    }

    public List<Map<String, Object>> getSalesTrend() {
        List<Map<String, Object>> raw = orderMapper.salesTrend7Days();
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

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : dateMap.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", entry.getKey());
            item.put("amount", entry.getValue());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getRecentOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        wrapper.ne(Order::getStatus, 6);
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> page = new Page<>(1, 10);
        Page<Order> result = orderMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Order o : result.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", o.getId());
            item.put("orderNo", o.getOrderNo());
            item.put("amount", o.getPayAmount());
            item.put("status", o.getStatus());
            item.put("time", o.getCreateTime());
            list.add(item);
        }
        return list;
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
