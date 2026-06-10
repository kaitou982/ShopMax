package com.shop.community.service;

import com.shop.common.web.PageResult;
import com.shop.community.controller.response.NoteResponse;

public interface NoteFavoriteService {

    boolean toggle(Long noteId, Long userId);

    PageResult<NoteResponse> pageByUserId(Long userId, Integer pageNum, Integer pageSize);
}
