package com.shop.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单Mapper
 *
 * @author shop
 * @since 2026-04-22
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM oms_order WHERE order_no = #{orderNo} AND deleted = 0")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单状态
     */
    @Update("UPDATE oms_order SET status = #{status}, update_time = NOW() WHERE id = #{orderId} AND deleted = 0")
    int updateStatus(@Param("orderId") Long orderId, @Param("status") Integer status);

    /**
     * 根据订单号更新订单状态
     */
    @Update("UPDATE oms_order SET status = #{status}, update_time = NOW() WHERE order_no = #{orderNo} AND deleted = 0")
    int updateStatusByOrderNo(@Param("orderNo") String orderNo, @Param("status") Integer status);

    /**
     * 统计用户订单数量
     */
    @Select("SELECT COUNT(*) FROM oms_order WHERE user_id = #{userId} AND deleted = 0")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 查询包含指定用户商品的订单ID列表（STORE数据隔离）
     */
    @Select("SELECT DISTINCT o.id FROM oms_order o " +
            "INNER JOIN oms_order_item oi ON o.id = oi.order_id AND oi.deleted = 0 " +
            "INNER JOIN pms_product p ON oi.product_id = p.id AND p.deleted = 0 " +
            "WHERE p.create_user_id = #{userId} AND o.deleted = 0")
    List<Long> selectOrderIdsByProductCreator(@Param("userId") Long userId);

    // ==================== 仪表盘统计 ====================

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
}
