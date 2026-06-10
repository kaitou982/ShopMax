package com.shop.customerservice.controller;

import com.shop.common.web.Result;
import com.shop.common.web.PageResult;
import com.shop.customerservice.entity.CsSession;
import com.shop.customerservice.service.CsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "客服管理（管理员）")
@RestController
@RequestMapping("/api/v1/cs/admin")
@RequiredArgsConstructor
public class CsAdminController {

    private final CsService csService;

    @Operation(summary = "管理员查看客服会话列表")
    @GetMapping("/sessions")
    public Result<PageResult<CsSession>> getSessions(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "20") Integer pageSize,
                                                      @RequestParam(required = false) Long userId) {
        return Result.success(csService.getAdminSessions(pageNum, pageSize, userId));
    }
}
