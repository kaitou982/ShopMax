package com.shop.customerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.customerservice.entity.CsSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CsSessionMapper extends BaseMapper<CsSession> {

    @Select("SELECT * FROM csms_session WHERE session_no = #{sessionNo} AND deleted = 0")
    CsSession selectBySessionNo(@Param("sessionNo") String sessionNo);

    @Select("SELECT * FROM csms_session WHERE user_id = #{userId} AND status = 0 AND deleted = 0 ORDER BY last_message_time DESC")
    List<CsSession> selectActiveByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM csms_session WHERE user_id = #{userId} AND status = 0 AND deleted = 0")
    Long countActiveByUserId(@Param("userId") Long userId);
}
