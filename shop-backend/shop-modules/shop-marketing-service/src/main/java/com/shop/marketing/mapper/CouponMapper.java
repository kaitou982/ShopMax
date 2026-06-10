package com.shop.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.marketing.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @Update("UPDATE mms_coupon SET received_count = received_count + 1 WHERE id = #{id} AND received_count < total_count AND deleted = 0")
    int increaseReceivedCount(@Param("id") Long id);

    @Update("UPDATE mms_coupon SET used_count = used_count + 1 WHERE id = #{id} AND deleted = 0")
    int increaseUsedCount(@Param("id") Long id);
}
