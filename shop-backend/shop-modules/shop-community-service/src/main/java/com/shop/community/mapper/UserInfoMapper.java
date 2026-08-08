package com.shop.community.mapper;

import com.shop.community.controller.response.NoteResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 社区服务只读查询本库数据的 Mapper。
 * 跨服务数据（用户信息、商品信息）已改为通过 Feign 调用，见 CommunityExternalService。
 */
@Mapper
public interface UserInfoMapper {

    @Select("<script>SELECT id, name, main_image AS mainImage, sale_price AS salePrice FROM pms_product WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> AND deleted = 0</script>")
    @Deprecated
    List<NoteResponse.ProductItem> selectProductItems(@Param("ids") List<Long> productIds);

    @Select("SELECT follow_user_id FROM ums_user_follow WHERE user_id = #{userId}")
    @Deprecated
    List<Long> selectFollowingUserIds(@Param("userId") Long userId);

    @Deprecated
    class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }
}
