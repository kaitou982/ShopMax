package com.shop.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.marketing.entity.GroupBuyGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GroupBuyGroupMapper extends BaseMapper<GroupBuyGroup> {

    @Update("UPDATE mms_group_buy_group SET current_count = current_count + 1 WHERE id = #{id} AND current_count < required_count AND deleted = 0")
    int incrementCurrentCount(@Param("id") Long id);
}
