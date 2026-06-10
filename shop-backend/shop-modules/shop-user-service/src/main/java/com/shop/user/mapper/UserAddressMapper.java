package com.shop.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.user.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户地址Mapper
 *
 * @author shop
 * @since 2026-04-15
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 查询用户地址列表
     */
    @Select("SELECT * FROM ums_user_address WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, create_time DESC")
    List<UserAddress> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户默认地址
     */
    @Select("SELECT * FROM ums_user_address WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0 LIMIT 1")
    UserAddress selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 取消用户所有默认地址
     */
    @Update("UPDATE ums_user_address SET is_default = 0 WHERE user_id = #{userId} AND deleted = 0")
    int cancelDefaultByUserId(@Param("userId") Long userId);

    /**
     * 统计用户地址数量
     */
    @Select("SELECT COUNT(*) FROM ums_user_address WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") Long userId);
}
