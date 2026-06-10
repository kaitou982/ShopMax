package com.shop.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface CouponReceiveMapper {

    @Update("UPDATE mms_coupon_receive SET status = 1, order_id = #{orderId}, order_no = #{orderNo}, use_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND status = 0 AND deleted = 0")
    int useCoupon(@Param("id") Long id, @Param("userId") Long userId, @Param("orderId") Long orderId, @Param("orderNo") String orderNo);

    /**
     * 查询用户持有的优惠券详情（含券定义信息），用于服务端校验
     */
    @Select("SELECT r.id, r.user_id, r.status, r.receive_time, r.coupon_id, " +
            "c.type AS coupon_type, c.min_amount, c.discount_amount, c.discount_rate, " +
            "c.valid_days, c.use_start_time, c.use_end_time, c.applicable_type, " +
            "c.applicable_ids, c.stackable, c.integral_cost " +
            "FROM mms_coupon_receive r JOIN mms_coupon c ON r.coupon_id = c.id " +
            "WHERE r.id = #{id} AND r.user_id = #{userId} AND r.deleted = 0 AND c.deleted = 0")
    Map<String, Object> selectCouponDetail(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 检查用户是否为首次下单（从未有过已支付或已完成的订单）
     */
    @Select("SELECT COUNT(1) FROM oms_order WHERE user_id = #{userId} AND status IN (1,2,3) AND deleted = 0")
    int countUserOrders(@Param("userId") Long userId);

    /** 退还优惠券（订单取消时恢复未使用状态） */
    @Update("UPDATE mms_coupon_receive SET status = 0, order_id = NULL, order_no = NULL, use_time = NULL WHERE id = #{id} AND user_id = #{userId} AND status = 1 AND deleted = 0")
    int restoreCoupon(@Param("id") Long id, @Param("userId") Long userId);
}
