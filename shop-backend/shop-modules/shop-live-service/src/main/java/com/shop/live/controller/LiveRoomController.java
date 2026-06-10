package com.shop.live.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.live.controller.request.LiveRoomCreateRequest;
import com.shop.live.controller.request.LiveRoomUpdateRequest;
import com.shop.live.controller.response.LiveRoomResponse;
import com.shop.live.service.LiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "直播间管理")
@RestController
@RequestMapping("/api/v1/live/rooms")
@RequiredArgsConstructor
public class LiveRoomController {

    private final LiveRoomService liveRoomService;

    @Operation(summary = "创建直播间")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<LiveRoomResponse> create(@Valid @RequestBody LiveRoomCreateRequest request) {
        return Result.success(liveRoomService.create(request));
    }

    @Operation(summary = "更新直播间")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<LiveRoomResponse> update(@PathVariable Long id,
                                           @Valid @RequestBody LiveRoomUpdateRequest request) {
        return Result.success(liveRoomService.update(id, request));
    }

    @Operation(summary = "删除直播间")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        liveRoomService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取直播间详情")
    @GetMapping("/{id}")
    public Result<LiveRoomResponse> getById(@PathVariable Long id) {
        return Result.success(liveRoomService.getById(id));
    }

    @Operation(summary = "分页查询直播间")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<LiveRoomResponse>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false) Integer type,
                                                      @RequestParam(required = false) Integer status) {
        return Result.success(liveRoomService.page(pageNum, pageSize, type, status));
    }

    @Operation(summary = "公开分页查询直播间（无需登录）")
    @GetMapping("/public")
    public Result<PageResult<LiveRoomResponse>> publicPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(liveRoomService.page(pageNum, pageSize, null, null));
    }

    @Operation(summary = "正在直播的房间列表")
    @GetMapping("/living")
    public Result<List<LiveRoomResponse>> listLiving() {
        return Result.success(liveRoomService.listLiving());
    }

    @Operation(summary = "开始直播")
    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<LiveRoomResponse> startLive(@PathVariable Long id) {
        return Result.success(liveRoomService.startLive(id));
    }

    @Operation(summary = "结束直播")
    @PutMapping("/{id}/end")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<LiveRoomResponse> endLive(@PathVariable Long id) {
        return Result.success(liveRoomService.endLive(id));
    }
}
