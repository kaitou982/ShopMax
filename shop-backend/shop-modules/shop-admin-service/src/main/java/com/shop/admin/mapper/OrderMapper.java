package com.shop.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.admin.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM oms_order WHERE deleted = 0 AND status IN (1,2,3) AND DATE(create_time) = CURDATE()")
    BigDecimal sumTodaySales();

    @Select("SELECT COALESCE(COUNT(*), 0) FROM oms_order WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    Long countTodayOrders();

    @Select("SELECT COALESCE(COUNT(*), 0) FROM oms_order WHERE deleted = 0 AND status = 0")
    Long countPendingOrders();

    @Select("SELECT DATE(create_time) as date, COALESCE(SUM(pay_amount), 0) as amount FROM oms_order WHERE deleted = 0 AND status IN (1,2,3) AND create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(create_time) ORDER BY DATE(create_time)")
    List<Map<String, Object>> salesTrend7Days();

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM oms_order WHERE deleted = 0 AND status IN (1,2,3) AND DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    BigDecimal sumYesterdaySales();

    @Select("SELECT COALESCE(COUNT(*), 0) FROM oms_order WHERE deleted = 0 AND DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long countYesterdayOrders();

    @Select("SELECT COALESCE(COUNT(*), 0) FROM oms_order WHERE deleted = 0 AND status = 0")
    Long countPendingOrdersCurrent();

    @Select("SELECT COALESCE(COUNT(*), 0) FROM oms_order WHERE deleted = 0 AND status = 0 AND DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long countYesterdayPendingOrders();
}
