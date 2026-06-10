package com.shop.community.mapper;

import com.shop.community.controller.response.NoteResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserInfoMapper {

    @Select("SELECT nickname, avatar FROM ums_user WHERE id = #{userId} AND deleted = 0")
    UserInfo selectUserInfo(@Param("userId") Long userId);

    @Select("<script>SELECT id, name, main_image AS mainImage, sale_price AS salePrice FROM pms_product WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> AND deleted = 0</script>")
    List<NoteResponse.ProductItem> selectProductItems(@Param("ids") List<Long> productIds);

    @Select("SELECT follow_user_id FROM ums_user_follow WHERE user_id = #{userId}")
    List<Long> selectFollowingUserIds(@Param("userId") Long userId);

    class UserInfo {
        private String nickname;
        private String avatar;

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }
}
