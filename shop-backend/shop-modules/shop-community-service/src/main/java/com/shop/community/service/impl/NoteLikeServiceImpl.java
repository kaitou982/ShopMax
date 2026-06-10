package com.shop.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.community.entity.NoteLike;
import com.shop.community.mapper.NoteLikeMapper;
import com.shop.community.mapper.NoteMapper;
import com.shop.community.service.NoteLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteLikeServiceImpl implements NoteLikeService {

    private final NoteLikeMapper noteLikeMapper;
    private final NoteMapper noteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long noteId, Long userId) {
        LambdaQueryWrapper<NoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteLike::getNoteId, noteId);
        wrapper.eq(NoteLike::getUserId, userId);

        NoteLike existing = noteLikeMapper.selectOne(wrapper);
        if (existing != null) {
            noteLikeMapper.deleteById(existing.getId());
            noteMapper.updateLikeCount(noteId, -1);
            log.info("取消点赞: noteId={}, userId={}", noteId, userId);
            return false;
        }

        NoteLike noteLike = new NoteLike();
        noteLike.setNoteId(noteId);
        noteLike.setUserId(userId);
        noteLikeMapper.insert(noteLike);
        noteMapper.updateLikeCount(noteId, 1);
        log.info("点赞: noteId={}, userId={}", noteId, userId);
        return true;
    }
}
