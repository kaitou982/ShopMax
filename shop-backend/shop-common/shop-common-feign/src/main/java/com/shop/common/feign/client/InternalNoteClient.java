package com.shop.common.feign.client;

import com.shop.common.feign.fallback.InternalNoteClientFallbackFactory;
import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "shop-community-service", contextId = "internalNoteClient",
             path = "/internal/notes", fallbackFactory = InternalNoteClientFallbackFactory.class)
public interface InternalNoteClient {

    @GetMapping("/page")
    Result<Map<String, Object>> pageNotes(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword);

    @GetMapping("/{id}/detail")
    Result<Map<String, Object>> getNoteDetail(@PathVariable("id") Long id);

    @PutMapping("/{id}/audit")
    Result<Map<String, Object>> auditNote(@PathVariable("id") Long id, @RequestBody Map<String, Object> request);

    @GetMapping("/stats/overview")
    Result<Map<String, Object>> getStatsOverview();
}
