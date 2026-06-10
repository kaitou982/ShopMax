package com.shop.live.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.common.web.PageResult;
import com.shop.live.controller.request.AnchorApplyRequest;
import com.shop.live.controller.request.AnchorAuditRequest;
import com.shop.live.controller.response.AnchorResponse;
import com.shop.live.entity.Anchor;

public interface AnchorService extends IService<Anchor> {

    AnchorResponse apply(Long userId, AnchorApplyRequest request);

    AnchorResponse audit(Long id, AnchorAuditRequest request);

    AnchorResponse getById(Long id);

    AnchorResponse getByUserId(Long userId);

    PageResult<AnchorResponse> page(Integer pageNum, Integer pageSize, Integer status);
}
