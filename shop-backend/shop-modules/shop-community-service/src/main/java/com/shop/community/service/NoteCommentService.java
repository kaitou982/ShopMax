package com.shop.community.service;

import com.shop.common.web.PageResult;
import com.shop.community.controller.request.CommentCreateRequest;
import com.shop.community.controller.response.CommentResponse;
import com.shop.community.entity.NoteComment;

public interface NoteCommentService {

    CommentResponse create(Long userId, Long noteId, CommentCreateRequest request);

    void delete(Long commentId, Long userId);

    PageResult<CommentResponse> pageByNoteId(Long noteId, Integer pageNum, Integer pageSize);
}
