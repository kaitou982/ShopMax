package com.shop.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.web.Result;
import com.shop.community.controller.request.NoteAuditRequest;
import com.shop.community.entity.Note;
import com.shop.community.entity.NoteStatus;
import com.shop.community.mapper.NoteMapper;
import com.shop.community.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 笔记内部接口（供 admin-service 通过 Feign 调用）
 */
@RestController
@RequestMapping("/internal/notes")
@RequiredArgsConstructor
public class InternalNoteController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;

    @GetMapping("/page")
    public Result<Map<String, Object>> pageNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();

        if (status != null && status != 0) {
            wrapper.eq(Note::getStatus, status);
        } else {
            wrapper.ne(Note::getStatus, 5);
        }

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(Note::getTitle, keyword)
                    .or()
                    .like(Note::getContent, keyword));
        }

        wrapper.orderByDesc(Note::getCreateTime);

        Page<Note> page = new Page<>(pageNum, pageSize);
        Page<Note> result = noteMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = result.getRecords().stream()
                .map(n -> {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("id", n.getId());
                    resp.put("userId", n.getUserId());
                    resp.put("title", n.getTitle());
                    resp.put("coverUrl", n.getCoverUrl());
                    resp.put("status", n.getStatus());
                    resp.put("likeCount", n.getLikeCount());
                    resp.put("commentCount", n.getCommentCount());
                    resp.put("viewCount", n.getViewCount());
                    resp.put("createTime", n.getCreateTime());
                    return resp;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());

        return Result.success(data);
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getNoteDetail(@PathVariable Long id) {
        try {
            var detail = noteService.getDetail(id, null);
            return Result.success(Map.of("detail", detail));
        } catch (Exception e) {
            return Result.error(404, "笔记不存在");
        }
    }

    @PutMapping("/{id}/audit")
    public Result<Map<String, Object>> auditNote(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer status = (Integer) request.get("status");
        String rejectReason = (String) request.get("rejectReason");
        try {
            var result = noteService.audit(id, status, rejectReason);
            return Result.success(Map.of("result", result));
        } catch (Exception e) {
            return Result.error(500, "审核失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getStatsOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        LambdaQueryWrapper<Note> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Note::getStatus, NoteStatus.UNDER_REVIEW.getCode());
        long pendingCount = noteMapper.selectCount(pendingWrapper);

        LambdaQueryWrapper<Note> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Note::getStatus, NoteStatus.PUBLISHED.getCode());
        approvedWrapper.ge(Note::getAuditTime, todayStart);
        long todayApproved = noteMapper.selectCount(approvedWrapper);

        LambdaQueryWrapper<Note> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(Note::getStatus, NoteStatus.REJECTED.getCode());
        rejectedWrapper.ge(Note::getAuditTime, todayStart);
        long todayRejected = noteMapper.selectCount(rejectedWrapper);

        LambdaQueryWrapper<Note> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.ne(Note::getStatus, 5);
        long totalCount = noteMapper.selectCount(totalWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingReviewCount", pendingCount);
        stats.put("todayApprovedCount", todayApproved);
        stats.put("todayRejectedCount", todayRejected);
        stats.put("totalNoteCount", totalCount);

        return Result.success(stats);
    }
}
