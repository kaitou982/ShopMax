package com.shop.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 优惠券领取记录 Mapper（order-service 本地）
 * 注意：分库后仅保留本地查询，跨库操作通过 Feign 调用 marketing-service
 */
@Mapper
public interface CouponReceiveMapper {

    /**
     * 检查用户是否为首次下单（从未有过已支付或已完成的订单）
     */
    @Select("SELECT COUNT(1) FROM oms_order WHERE user_id = #{userId} AND status IN (1,2,3) AND deleted = 0")
    int countUserOrders(@Param("userId") Long userId);
}
