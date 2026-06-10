package com.shop.community.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.community.controller.request.CommentCreateRequest;
import com.shop.community.controller.request.NoteCreateRequest;
import com.shop.community.controller.request.NoteUpdateRequest;
import com.shop.community.controller.response.CommentResponse;
import com.shop.community.controller.response.NoteDetailResponse;
import com.shop.community.controller.response.NoteResponse;
import com.shop.community.service.NoteCommentService;
import com.shop.community.service.NoteFavoriteService;
import com.shop.community.service.NoteLikeService;
import com.shop.community.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "内容社区")
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final NoteLikeService noteLikeService;
    private final NoteCommentService noteCommentService;
    private final NoteFavoriteService noteFavoriteService;

    @Operation(summary = "发布笔记")
    @PostMapping("/notes")
    public Result<NoteDetailResponse> create(@Valid @RequestBody NoteCreateRequest request) {
        Long userId = getCurrentUserIdOrThrow();
        return Result.success(noteService.create(userId, request));
    }

    @Operation(summary = "编辑笔记")
    @PutMapping("/notes/{id}")
    public Result<NoteDetailResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody NoteUpdateRequest request) {
        Long userId = getCurrentUserIdOrThrow();
        return Result.success(noteService.update(userId, id, request));
    }

    @Operation(summary = "删除笔记")
    @DeleteMapping("/notes/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserIdOrThrow();
        noteService.delete(userId, id);
        return Result.success();
    }

    @Operation(summary = "笔记详情")
    @GetMapping("/notes/{id}")
    public Result<NoteDetailResponse> getDetail(@PathVariable Long id) {
        return Result.success(noteService.getDetail(id));
    }

    @Operation(summary = "笔记列表（推荐/关注）")
    @GetMapping("/notes")
    public Result<PageResult<NoteResponse>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "recommend") String tab) {
        Long currentUserId = getCurrentUserId();
        return Result.success(noteService.page(pageNum, pageSize, tab, currentUserId));
    }

    @Operation(summary = "用户笔记列表")
    @GetMapping("/users/{userId}/notes")
    public Result<PageResult<NoteResponse>> pageByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(noteService.pageByUserId(userId, pageNum, pageSize));
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/notes/{id}/like")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        Long userId = getCurrentUserIdOrThrow();
        return Result.success(noteLikeService.toggle(id, userId));
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/notes/{id}/favorite")
    public Result<Boolean> toggleFavorite(@PathVariable Long id) {
        Long userId = getCurrentUserIdOrThrow();
        return Result.success(noteFavoriteService.toggle(id, userId));
    }

    @Operation(summary = "获取评论列表")
    @GetMapping("/notes/{id}/comments")
    public Result<PageResult<CommentResponse>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(noteCommentService.pageByNoteId(id, pageNum, pageSize));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/notes/{id}/comments")
    public Result<CommentResponse> createComment(@PathVariable Long id,
                                                  @Valid @RequestBody CommentCreateRequest request) {
        Long userId = getCurrentUserIdOrThrow();
        return Result.success(noteCommentService.create(userId, id, request));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = getCurrentUserIdOrThrow();
        noteCommentService.delete(id, userId);
        return Result.success();
    }

    @Operation(summary = "我的收藏")
    @GetMapping("/users/me/favorites")
    public Result<PageResult<NoteResponse>> myFavorites(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserIdOrThrow();
        return Result.success(noteFavoriteService.pageByUserId(userId, pageNum, pageSize));
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private Long getCurrentUserIdOrThrow() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new com.shop.common.exception.BusinessException(401, "请先登录");
        }
        return userId;
    }
}
