package com.shop.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.user.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @Select("SELECT * FROM mms_coupon WHERE type = 4 AND status = 1 AND deleted = 0")
    List<Coupon> findActiveNewUserCoupons();

    @Update("UPDATE mms_coupon SET received_count = received_count + 1 WHERE id = #{id} AND received_count < total_count AND deleted = 0")
    int increaseReceivedCount(@Param("id") Long id);
}
