package com.shop.live.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.live.entity.LiveRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LiveRoomMapper extends BaseMapper<LiveRoom> {

    @Update("UPDATE lms_live_room SET total_view_count = total_view_count + 1, online_count = online_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementViewCount(@Param("id") Long id);

    @Update("UPDATE lms_live_room SET like_count = like_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementLikeCount(@Param("id") Long id);
}
