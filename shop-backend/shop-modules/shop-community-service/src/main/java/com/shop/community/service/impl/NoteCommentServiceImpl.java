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
import com.shop.community.service.CommunityExternalService;
import com.shop.community.service.NoteCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteCommentServiceImpl implements NoteCommentService {

    private final NoteCommentMapper noteCommentMapper;
    private final NoteMapper noteMapper;
    private final CommunityExternalService externalService;

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

        // 级联删除所有子回复
        LambdaQueryWrapper<NoteComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(NoteComment::getParentId, commentId);
        long childCount = noteCommentMapper.selectCount(childWrapper);
        noteCommentMapper.delete(childWrapper);

        noteCommentMapper.deleteById(commentId);
        noteMapper.updateCommentCount(comment.getNoteId(), -(int) (1 + childCount));
        log.info("删除评论: commentId={}, 子评论数={}", commentId, childCount);
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

        // 收集所有需要查询的 userId
        Set<Long> allUserIds = new java.util.HashSet<>();
        result.getRecords().forEach(c -> {
            allUserIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) allUserIds.add(c.getReplyToUserId());
        });

        Map<Long, List<CommentResponse>> childrenMap = Map.of();
        if (!parentIds.isEmpty()) {
            LambdaQueryWrapper<NoteComment> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.in(NoteComment::getParentId, parentIds);
            childWrapper.orderByAsc(NoteComment::getCreateTime);
            List<NoteComment> children = noteCommentMapper.selectList(childWrapper);
            children.forEach(c -> {
                allUserIds.add(c.getUserId());
                if (c.getReplyToUserId() != null) allUserIds.add(c.getReplyToUserId());
            });

            Map<Long, CommunityExternalService.UserInfo> userInfoMap = batchLoadUsers(allUserIds);

            childrenMap = children.stream()
                    .map(c -> buildResponseWithCache(c, userInfoMap))
                    .collect(Collectors.groupingBy(CommentResponse::getParentId));
        }

        Map<Long, CommunityExternalService.UserInfo> userInfoMap = batchLoadUsers(allUserIds);
        Map<Long, List<CommentResponse>> finalChildrenMap = childrenMap;
        List<CommentResponse> records = result.getRecords().stream()
                .map(comment -> {
                    CommentResponse resp = buildResponseWithCache(comment, userInfoMap);
                    resp.setChildren(finalChildrenMap.getOrDefault(comment.getId(), List.of()));
                    return resp;
                })
                .collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getPages(), result.getCurrent(), result.getSize());
    }

    private Map<Long, CommunityExternalService.UserInfo> batchLoadUsers(Set<Long> userIds) {
        userIds.remove(null);
        if (userIds.isEmpty()) return Map.of();
        return externalService.batchGetUserInfo(new ArrayList<>(userIds));
    }

    private CommentResponse buildResponse(NoteComment comment) {
        Set<Long> ids = new java.util.HashSet<>();
        ids.add(comment.getUserId());
        if (comment.getReplyToUserId() != null) ids.add(comment.getReplyToUserId());
        return buildResponseWithCache(comment, batchLoadUsers(ids));
    }

    private CommentResponse buildResponseWithCache(NoteComment comment, Map<Long, CommunityExternalService.UserInfo> userInfoMap) {
        CommentResponse resp = new CommentResponse();
        resp.setId(comment.getId());
        resp.setNoteId(comment.getNoteId());
        resp.setUserId(comment.getUserId());
        resp.setParentId(comment.getParentId());
        resp.setReplyToUserId(comment.getReplyToUserId());
        resp.setContent(comment.getContent());
        resp.setLikeCount(comment.getLikeCount());
        resp.setCreateTime(comment.getCreateTime());

        var userInfo = userInfoMap.get(comment.getUserId());
        if (userInfo != null) {
            resp.setUserNickname(userInfo.nickname());
            resp.setUserAvatar(userInfo.avatar());
        }

        if (comment.getReplyToUserId() != null) {
            var replyUserInfo = userInfoMap.get(comment.getReplyToUserId());
            if (replyUserInfo != null) {
                resp.setReplyToUserNickname(replyUserInfo.nickname());
            }
        }

        return resp;
    }
}
