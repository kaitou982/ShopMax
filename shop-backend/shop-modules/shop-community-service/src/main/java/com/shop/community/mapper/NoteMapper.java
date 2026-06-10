package com.shop.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.community.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Update("UPDATE cms_note SET view_count = view_count + #{count} WHERE id = #{noteId} AND deleted = 0")
    int increaseViewCount(@Param("noteId") Long noteId, @Param("count") int count);

    @Update("UPDATE cms_note SET like_count = like_count + #{delta} WHERE id = #{noteId} AND deleted = 0")
    int updateLikeCount(@Param("noteId") Long noteId, @Param("delta") int delta);

    @Update("UPDATE cms_note SET comment_count = comment_count + #{delta} WHERE id = #{noteId} AND deleted = 0")
    int updateCommentCount(@Param("noteId") Long noteId, @Param("delta") int delta);

    @Update("UPDATE cms_note SET favorite_count = favorite_count + #{delta} WHERE id = #{noteId} AND deleted = 0")
    int updateFavoriteCount(@Param("noteId") Long noteId, @Param("delta") int delta);
}
