package com.shop.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.web.PageResult;
import com.shop.community.controller.response.NoteResponse;
import com.shop.community.entity.Note;
import com.shop.community.entity.NoteFavorite;
import com.shop.community.mapper.*;
import com.shop.community.service.NoteFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFavoriteServiceImpl implements NoteFavoriteService {

    private final NoteFavoriteMapper noteFavoriteMapper;
    private final NoteMapper noteMapper;
    private final NoteImageMapper noteImageMapper;
    private final UserInfoMapper userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long noteId, Long userId) {
        LambdaQueryWrapper<NoteFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteFavorite::getNoteId, noteId);
        wrapper.eq(NoteFavorite::getUserId, userId);

        NoteFavorite existing = noteFavoriteMapper.selectOne(wrapper);
        if (existing != null) {
            noteFavoriteMapper.deleteById(existing.getId());
            noteMapper.updateFavoriteCount(noteId, -1);
            log.info("取消收藏: noteId={}, userId={}", noteId, userId);
            return false;
        }

        NoteFavorite favorite = new NoteFavorite();
        favorite.setNoteId(noteId);
        favorite.setUserId(userId);
        noteFavoriteMapper.insert(favorite);
        noteMapper.updateFavoriteCount(noteId, 1);
        log.info("收藏: noteId={}, userId={}", noteId, userId);
        return true;
    }

    @Override
    public PageResult<NoteResponse> pageByUserId(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<NoteFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteFavorite::getUserId, userId);
        wrapper.orderByDesc(NoteFavorite::getCreateTime);

        Page<NoteFavorite> page = new Page<>(pageNum, pageSize);
        Page<NoteFavorite> result = noteFavoriteMapper.selectPage(page, wrapper);

        List<Long> noteIds = result.getRecords().stream()
                .map(NoteFavorite::getNoteId)
                .collect(Collectors.toList());

        if (noteIds.isEmpty()) {
            return PageResult.empty();
        }

        List<Note> notes = noteMapper.selectBatchIds(noteIds);
        List<NoteResponse> records = notes.stream()
                .map(note -> {
                    NoteResponse resp = new NoteResponse();
                    resp.setId(note.getId());
                    resp.setUserId(note.getUserId());
                    resp.setTitle(note.getTitle());
                    resp.setCoverUrl(note.getCoverUrl());
                    resp.setLikeCount(note.getLikeCount());
                    resp.setCommentCount(note.getCommentCount());
                    resp.setCreateTime(note.getCreateTime());

                    var userInfo = userInfoMapper.selectUserInfo(note.getUserId());
                    if (userInfo != null) {
                        resp.setUserNickname(userInfo.getNickname());
                        resp.setUserAvatar(userInfo.getAvatar());
                    }

                    LambdaQueryWrapper<com.shop.community.entity.NoteImage> imgWrapper = new LambdaQueryWrapper<>();
                    imgWrapper.eq(com.shop.community.entity.NoteImage::getNoteId, note.getId());
                    imgWrapper.orderByAsc(com.shop.community.entity.NoteImage::getSortOrder);
                    resp.setImages(noteImageMapper.selectList(imgWrapper).stream()
                            .map(com.shop.community.entity.NoteImage::getImageUrl)
                            .collect(Collectors.toList()));

                    return resp;
                })
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }
}
