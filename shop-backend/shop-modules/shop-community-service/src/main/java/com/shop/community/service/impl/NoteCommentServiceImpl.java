package com.shop.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.exception.BusinessException;
import com.shop.common.web.PageResult;
import com.shop.community.controller.request.CommentCreateRequest;
import com.shop.community.controller.response.CommentResponse;
import com.shop.community.entity.NoteComment;
import com.shop.community.mapper.NoteCommentMapper;
import com.shop.community.mapper.NoteMapper;
import com.shop.community.mapper.UserInfoMapper;
import com.shop.community.service.NoteCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteCommentServiceImpl implements NoteCommentService {

    private final NoteCommentMapper noteCommentMapper;
    private final NoteMapper noteMapper;
    private final UserInfoMapper userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse create(Long userId, Long noteId, CommentCreateRequest request) {
        NoteComment comment = new NoteComment();
        comment.setNoteId(noteId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(request.getReplyToUserId());

        if (request.getParentId() != null) {
            NoteComment parent = noteCommentMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException("父评论不存在");
            }
        }

        noteCommentMapper.insert(comment);
        noteMapper.updateCommentCount(noteId, 1);
        log.info("发表评论: noteId={}, commentId={}, userId={}", noteId, comment.getId(), userId);

        return buildResponse(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long commentId, Long userId) {
        NoteComment comment = noteCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }

        noteCommentMapper.deleteById(commentId);
        noteMapper.updateCommentCount(comment.getNoteId(), -1);
        log.info("删除评论: commentId={}", commentId);
    }

    @Override
    public PageResult<CommentResponse> pageByNoteId(Long noteId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<NoteComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteComment::getNoteId, noteId);
        wrapper.isNull(NoteComment::getParentId);
        wrapper.orderByDesc(NoteComment::getCreateTime);

        Page<NoteComment> page = new Page<>(pageNum, pageSize);
        Page<NoteComment> result = noteCommentMapper.selectPage(page, wrapper);

        List<Long> parentIds = result.getRecords().stream()
                .map(NoteComment::getId)
                .collect(Collectors.toList());

        Map<Long, List<CommentResponse>> childrenMap = Map.of();
        if (!parentIds.isEmpty()) {
            LambdaQueryWrapper<NoteComment> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.in(NoteComment::getParentId, parentIds);
            childWrapper.orderByAsc(NoteComment::getCreateTime);
            List<NoteComment> children = noteCommentMapper.selectList(childWrapper);
            childrenMap = children.stream()
                    .map(this::buildResponse)
                    .collect(Collectors.groupingBy(CommentResponse::getParentId));
        }

        Map<Long, List<CommentResponse>> finalChildrenMap = childrenMap;
        List<CommentResponse> records = result.getRecords().stream()
                .map(comment -> {
                    CommentResponse resp = buildResponse(comment);
                    resp.setChildren(finalChildrenMap.getOrDefault(comment.getId(), List.of()));
                    return resp;
                })
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    private CommentResponse buildResponse(NoteComment comment) {
        CommentResponse resp = new CommentResponse();
        resp.setId(comment.getId());
        resp.setNoteId(comment.getNoteId());
        resp.setUserId(comment.getUserId());
        resp.setParentId(comment.getParentId());
        resp.setReplyToUserId(comment.getReplyToUserId());
        resp.setContent(comment.getContent());
        resp.setLikeCount(comment.getLikeCount());
        resp.setCreateTime(comment.getCreateTime());

        var userInfo = userInfoMapper.selectUserInfo(comment.getUserId());
        if (userInfo != null) {
            resp.setUserNickname(userInfo.getNickname());
            resp.setUserAvatar(userInfo.getAvatar());
        }

        if (comment.getReplyToUserId() != null) {
            var replyUserInfo = userInfoMapper.selectUserInfo(comment.getReplyToUserId());
            if (replyUserInfo != null) {
                resp.setReplyToUserNickname(replyUserInfo.getNickname());
            }
        }

        return resp;
    }
}
