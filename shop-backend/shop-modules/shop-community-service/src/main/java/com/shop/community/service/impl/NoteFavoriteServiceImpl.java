package com.shop.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.web.PageResult;
import com.shop.community.controller.response.NoteResponse;
import com.shop.community.entity.Note;
import com.shop.community.entity.NoteFavorite;
import com.shop.community.mapper.*;
import com.shop.community.service.CommunityExternalService;
import com.shop.community.service.NoteFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFavoriteServiceImpl implements NoteFavoriteService {

    private final NoteFavoriteMapper noteFavoriteMapper;
    private final NoteMapper noteMapper;
    private final NoteImageMapper noteImageMapper;
    private final CommunityExternalService externalService;

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

        // 构建 noteId → 收藏时间 映射
        Map<Long, java.time.LocalDateTime> favoriteTimeMap = result.getRecords().stream()
                .collect(Collectors.toMap(NoteFavorite::getNoteId, NoteFavorite::getCreateTime, (a, b) -> a));

        // selectBatchIds 不保持顺序，需手动重排
        List<Note> notes = noteMapper.selectBatchIds(noteIds);
        Map<Long, Note> noteMap = notes.stream()
                .collect(Collectors.toMap(Note::getId, n -> n, (a, b) -> a));

        // 批量获取用户信息（通过 Feign）
        List<Long> userIds = notes.stream().map(Note::getUserId).distinct().collect(Collectors.toList());
        Map<Long, CommunityExternalService.UserInfo> userInfoMap = externalService.batchGetUserInfo(userIds);

        // 批量获取图片
        LambdaQueryWrapper<com.shop.community.entity.NoteImage> imgWrapper = new LambdaQueryWrapper<>();
        imgWrapper.in(com.shop.community.entity.NoteImage::getNoteId, noteIds);
        imgWrapper.orderByAsc(com.shop.community.entity.NoteImage::getSortOrder);
        Map<Long, List<String>> imageMap = noteImageMapper.selectList(imgWrapper).stream()
                .collect(Collectors.groupingBy(com.shop.community.entity.NoteImage::getNoteId,
                        Collectors.mapping(com.shop.community.entity.NoteImage::getImageUrl, Collectors.toList())));

        // 按 noteIds 顺序构建响应（保持收藏时间倒序）
        List<NoteResponse> records = new java.util.ArrayList<>();
        for (Long noteId : noteIds) {
            Note note = noteMap.get(noteId);
            if (note == null) continue;
            NoteResponse resp = new NoteResponse();
            resp.setId(note.getId());
            resp.setUserId(note.getUserId());
            resp.setTitle(note.getTitle());
            resp.setCoverUrl(note.getCoverUrl());
            resp.setLikeCount(note.getLikeCount());
            resp.setCommentCount(note.getCommentCount());
            resp.setFavoriteCount(note.getFavoriteCount());
            resp.setViewCount(note.getViewCount());
            // 收藏列表：createTime 显示收藏时间而非笔记发布时间
            var favTime = favoriteTimeMap.get(noteId);
            resp.setCreateTime(favTime != null ? favTime : note.getCreateTime());
            resp.setIsFavorited(true);

            var userInfo = userInfoMap.get(note.getUserId());
            if (userInfo != null) {
                resp.setUserNickname(userInfo.nickname());
                resp.setUserAvatar(userInfo.avatar());
            }

            resp.setImages(imageMap.getOrDefault(note.getId(), List.of()));
            records.add(resp);
        }

        return PageResult.of(records, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }
}
