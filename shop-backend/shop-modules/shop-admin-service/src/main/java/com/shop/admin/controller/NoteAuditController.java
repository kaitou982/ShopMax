package com.shop.admin.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.community.controller.request.NoteAuditRequest;
import com.shop.community.controller.response.NoteDetailResponse;
import com.shop.community.controller.response.NoteResponse;
import com.shop.community.controller.response.StatsOverviewResponse;
import com.shop.community.entity.NoteStatus;
import com.shop.community.mapper.NoteMapper;
import com.shop.community.service.NoteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容社区管理控制器
 *
 * @author shop
 * @since 2026-05-25
 */
@Tag(name = "内容社区管理")
@RestController
@RequestMapping("/api/v1/admin/community")
@RequiredArgsConstructor
public class NoteAuditController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;

    @Operation(summary = "审核笔记列表")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @GetMapping("/notes")
    public Result<PageResult<NoteResponse>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<com.shop.community.entity.Note> wrapper = new LambdaQueryWrapper<>();

        if (status != null && status != 0) {
            wrapper.eq(com.shop.community.entity.Note::getStatus, status);
        } else {
            wrapper.ne(com.shop.community.entity.Note::getStatus, 5);
        }

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(com.shop.community.entity.Note::getTitle, keyword)
                    .or()
                    .like(com.shop.community.entity.Note::getContent, keyword));
        }

        wrapper.orderByDesc(com.shop.community.entity.Note::getCreateTime);

        Page<com.shop.community.entity.Note> page = new Page<>(pageNum, pageSize);
        Page<com.shop.community.entity.Note> result = noteMapper.selectPage(page, wrapper);

        List<NoteResponse> records = result.getRecords().stream()
                .map(n -> {
                    NoteResponse resp = new NoteResponse();
                    resp.setId(n.getId());
                    resp.setUserId(n.getUserId());
                    resp.setTitle(n.getTitle());
                    resp.setCoverUrl(n.getCoverUrl());
                    resp.setStatus(n.getStatus());
                    resp.setLikeCount(n.getLikeCount());
                    resp.setCommentCount(n.getCommentCount());
                    resp.setViewCount(n.getViewCount());
                    resp.setCreateTime(n.getCreateTime());
                    return resp;
                })
                .collect(Collectors.toList());

        return Result.success(PageResult.of(records, result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "审核笔记详情")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @GetMapping("/notes/{id}")
    public Result<NoteDetailResponse> getDetail(@PathVariable Long id) {
        return Result.success(noteService.getDetail(id));
    }

    @Operation(summary = "审核操作")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/notes/{id}/audit")
    public Result<NoteDetailResponse> audit(@PathVariable Long id,
                                             @Valid @RequestBody NoteAuditRequest request) {
        return Result.success(noteService.audit(id, request.getStatus(), request.getRejectReason()));
    }

    @Operation(summary = "数据概览")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @GetMapping("/stats/overview")
    public Result<StatsOverviewResponse> overview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        LambdaQueryWrapper<com.shop.community.entity.Note> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(com.shop.community.entity.Note::getStatus, NoteStatus.UNDER_REVIEW.getCode());
        long pendingCount = noteMapper.selectCount(pendingWrapper);

        LambdaQueryWrapper<com.shop.community.entity.Note> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(com.shop.community.entity.Note::getStatus, NoteStatus.PUBLISHED.getCode());
        approvedWrapper.ge(com.shop.community.entity.Note::getAuditTime, todayStart);
        long todayApproved = noteMapper.selectCount(approvedWrapper);

        LambdaQueryWrapper<com.shop.community.entity.Note> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(com.shop.community.entity.Note::getStatus, NoteStatus.REJECTED.getCode());
        rejectedWrapper.ge(com.shop.community.entity.Note::getAuditTime, todayStart);
        long todayRejected = noteMapper.selectCount(rejectedWrapper);

        LambdaQueryWrapper<com.shop.community.entity.Note> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.ne(com.shop.community.entity.Note::getStatus, 5);
        long totalCount = noteMapper.selectCount(totalWrapper);

        return Result.success(StatsOverviewResponse.builder()
                .pendingReviewCount(pendingCount)
                .todayApprovedCount(todayApproved)
                .todayRejectedCount(todayRejected)
                .totalNoteCount(totalCount)
                .build());
    }
}
