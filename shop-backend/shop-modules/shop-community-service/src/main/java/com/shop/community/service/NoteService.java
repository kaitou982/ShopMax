package com.shop.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.community.controller.request.NoteCreateRequest;
import com.shop.community.controller.request.NoteUpdateRequest;
import com.shop.community.controller.response.NoteDetailResponse;
import com.shop.community.controller.response.NoteResponse;
import com.shop.community.entity.Note;

public interface NoteService extends IService<Note> {

    NoteDetailResponse create(Long userId, NoteCreateRequest request);

    NoteDetailResponse update(Long userId, Long noteId, NoteUpdateRequest request);

    void delete(Long userId, Long noteId);

    NoteDetailResponse getDetail(Long noteId, Long currentUserId);

    PageResult<NoteResponse> page(Integer pageNum, Integer pageSize, String tab, Long currentUserId);

    PageResult<NoteResponse> pageByUserId(Long userId, Integer pageNum, Integer pageSize);

    NoteDetailResponse audit(Long noteId, Integer status, String rejectReason);
}
