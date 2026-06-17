package com.shop.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.marketing.entity.SeckillMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀本地消息Mapper
 *
 * @author shop
 * @since 2026-06-15
 */
@Mapper
public interface SeckillMessageMapper extends BaseMapper<SeckillMessage> {

    /**
     * 查询待处理的消息
     */
    @Select("SELECT * FROM mms_seckill_message WHERE status = 0 AND deleted = 0 AND (next_retry_time IS NULL OR next_retry_time <= #{now}) LIMIT #{limit}")
    List<SeckillMessage> selectPendingMessages(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
