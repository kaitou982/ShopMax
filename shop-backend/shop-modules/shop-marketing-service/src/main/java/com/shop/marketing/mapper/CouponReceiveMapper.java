package com.shop.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.marketing.entity.CouponReceive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface CouponReceiveMapper extends BaseMapper<CouponReceive> {

    @Select("SELECT COUNT(*) FROM mms_coupon_receive WHERE coupon_id = #{couponId} AND user_id = #{userId} AND deleted = 0")
    int countByCouponIdAndUserId(@Param("couponId") Long couponId, @Param("userId") Long userId);

    @Update("UPDATE mms_coupon_receive SET status = 1, order_id = #{orderId}, order_no = #{orderNo}, use_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND status = 0 AND deleted = 0")
    int useCoupon(@Param("id") Long id, @Param("userId") Long userId, @Param("orderId") Long orderId, @Param("orderNo") String orderNo);

    @Select("SELECT r.id, r.user_id, r.status, r.receive_time, r.coupon_id, " +
            "c.type AS coupon_type, c.min_amount, c.discount_amount, c.discount_rate, " +
            "c.valid_days, c.use_start_time, c.use_end_time, c.applicable_type, " +
            "c.applicable_ids, c.stackable, c.integral_cost " +
            "FROM mms_coupon_receive r JOIN mms_coupon c ON r.coupon_id = c.id " +
            "WHERE r.id = #{id} AND r.user_id = #{userId} AND r.deleted = 0 AND c.deleted = 0")
    Map<String, Object> selectCouponDetail(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE mms_coupon_receive SET status = 0, order_id = NULL, order_no = NULL, use_time = NULL WHERE id = #{id} AND user_id = #{userId} AND status = 1 AND deleted = 0")
    int restoreCoupon(@Param("id") Long id, @Param("userId") Long userId);
}
