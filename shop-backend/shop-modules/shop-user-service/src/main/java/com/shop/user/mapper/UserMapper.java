package com.shop.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 *
 * @author shop
 * @since 2026-04-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM ums_user WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM ums_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据openid查询用户
     */
    @Select("SELECT * FROM ums_user WHERE openid_mp = #{openid} AND deleted = 0")
    User selectByOpenid(@Param("openid") String openid);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM ums_user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);
}
