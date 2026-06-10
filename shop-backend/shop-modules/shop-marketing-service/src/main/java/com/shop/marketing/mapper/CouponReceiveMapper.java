package com.shop.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.marketing.entity.CouponReceive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CouponReceiveMapper extends BaseMapper<CouponReceive> {

    @Select("SELECT COUNT(*) FROM mms_coupon_receive WHERE coupon_id = #{couponId} AND user_id = #{userId} AND deleted = 0")
    int countByCouponIdAndUserId(@Param("couponId") Long couponId, @Param("userId") Long userId);
}
