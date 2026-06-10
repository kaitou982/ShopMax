package com.shop.customerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.customerservice.entity.CsMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CsMessageMapper extends BaseMapper<CsMessage> {

    @Select("SELECT * FROM csms_message WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY create_time ASC")
    List<CsMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    @Select("SELECT COUNT(*) FROM csms_message WHERE session_id = #{sessionId} AND deleted = 0")
    Long countBySessionId(@Param("sessionId") Long sessionId);

    @Select("SELECT * FROM csms_message WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<CsMessage> selectRecentBySessionId(@Param("sessionId") Long sessionId, @Param("limit") Integer limit);
}
