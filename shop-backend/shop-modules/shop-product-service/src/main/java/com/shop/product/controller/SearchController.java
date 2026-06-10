package com.shop.product.controller;

import com.shop.common.web.Result;
import com.shop.product.controller.response.HotKeywordResponse;
import com.shop.product.controller.response.SuggestResponse;
import com.shop.product.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 搜索控制器
 *
 * @author shop
 * @since 2026-05-31
 */
@Tag(name = "搜索管理")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "记录搜索关键词")
    @PostMapping("/record")
    public Result<Void> record(@RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        Long userId = getCurrentUserId();
        searchService.recordKeyword(keyword, userId);
        return Result.success();
    }

    @Operation(summary = "获取热门搜索")
    @GetMapping("/hot")
    public Result<List<HotKeywordResponse>> hot(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(searchService.getHotKeywords(limit));
    }

    @Operation(summary = "获取搜索建议")
    @GetMapping("/suggest")
    public Result<SuggestResponse> suggest(@RequestParam String keyword,
                                           @RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(searchService.getSuggestions(keyword, limit));
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
