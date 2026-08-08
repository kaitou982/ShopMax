package com.shop.admin.controller;

import com.shop.common.feign.client.InternalNoteClient;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 内容社区管理控制器（通过 Feign 调用 community-service）
 *
 * @author shop
 * @since 2026-05-25
 */
@Tag(name = "内容社区管理")
@RestController
@RequestMapping("/api/v1/admin/community")
@RequiredArgsConstructor
public class NoteAuditController {

    private final InternalNoteClient internalNoteClient;

    @Operation(summary = "审核笔记列表")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @GetMapping("/notes")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return internalNoteClient.pageNotes(pageNum, pageSize, status, keyword);
    }

    @Operation(summary = "审核笔记详情")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @GetMapping("/notes/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        return internalNoteClient.getNoteDetail(id);
    }

    @Operation(summary = "审核操作")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/notes/{id}/audit")
    public Result<Map<String, Object>> audit(@PathVariable Long id,
                                             @RequestBody Map<String, Object> request) {
        return internalNoteClient.auditNote(id, request);
    }

    @Operation(summary = "数据概览")
    @PreAuthorize("hasAnyRole('ADMIN','STORE')")
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> overview() {
        return internalNoteClient.getStatsOverview();
    }
}
