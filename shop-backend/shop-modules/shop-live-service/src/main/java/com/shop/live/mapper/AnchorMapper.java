package com.shop.live.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.live.entity.Anchor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AnchorMapper extends BaseMapper<Anchor> {

    @Update("UPDATE lms_anchor SET fans_count = fans_count + 1 WHERE id = #{id} AND deleted = 0")
    int increaseFansCount(@Param("id") Long id);

    @Update("UPDATE lms_anchor SET fans_count = fans_count - 1 WHERE id = #{id} AND fans_count > 0 AND deleted = 0")
    int decreaseFansCount(@Param("id") Long id);
}
