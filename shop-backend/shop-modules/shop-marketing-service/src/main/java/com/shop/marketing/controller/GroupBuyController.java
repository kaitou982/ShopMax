package com.shop.marketing.controller;

import com.shop.common.web.PageResult;
import com.shop.common.web.Result;
import com.shop.marketing.controller.request.GroupBuyActivityCreateRequest;
import com.shop.marketing.controller.request.JoinGroupRequest;
import com.shop.marketing.controller.request.StartGroupRequest;
import com.shop.marketing.controller.response.GroupBuyActivityResponse;
import com.shop.marketing.controller.response.GroupBuyGroupResponse;
import com.shop.marketing.service.GroupBuyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "拼团管理")
@RestController
@RequestMapping("/api/v1/marketing/group-buy")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;

    @Operation(summary = "创建拼团活动")
    @PostMapping("/activities")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<GroupBuyActivityResponse> createActivity(@Valid @RequestBody GroupBuyActivityCreateRequest request) {
        return Result.success(groupBuyService.createActivity(request));
    }

    @Operation(summary = "拼团活动列表")
    @GetMapping("/activities")
    public Result<PageResult<GroupBuyActivityResponse>> pageActivities(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(groupBuyService.pageActivities(pageNum, pageSize));
    }

    @Operation(summary = "进行中的拼团活动")
    @GetMapping("/activities/active")
    public Result<List<GroupBuyActivityResponse>> listActive() {
        return Result.success(groupBuyService.listActive());
    }

    @Operation(summary = "发起拼团")
    @PostMapping("/groups/start")
    public Result<GroupBuyGroupResponse> startGroup(@RequestAttribute("userId") Long userId,
                                                     @Valid @RequestBody StartGroupRequest request) {
        return Result.success(groupBuyService.startGroup(request.getActivityId(), userId));
    }

    @Operation(summary = "参加拼团")
    @PostMapping("/groups/{id}/join")
    public Result<GroupBuyGroupResponse> joinGroup(@PathVariable Long id,
                                                    @RequestAttribute("userId") Long userId,
                                                    @Valid @RequestBody JoinGroupRequest request) {
        return Result.success(groupBuyService.joinGroup(id, userId));
    }

    @Operation(summary = "拼团详情")
    @GetMapping("/groups/{id}")
    public Result<GroupBuyGroupResponse> getGroupDetail(@PathVariable Long id) {
        return Result.success(groupBuyService.getGroupDetail(id));
    }

    @Operation(summary = "我的拼团")
    @GetMapping("/groups/my")
    public Result<PageResult<GroupBuyGroupResponse>> myGroups(@RequestAttribute("userId") Long userId,
                                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(groupBuyService.pageMyGroups(userId, pageNum, pageSize));
    }
}
