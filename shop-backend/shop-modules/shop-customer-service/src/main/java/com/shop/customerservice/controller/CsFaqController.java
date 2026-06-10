package com.shop.customerservice.controller;

import com.shop.common.web.Result;
import com.shop.common.web.PageResult;
import com.shop.customerservice.dto.FaqImportRequest;
import com.shop.customerservice.entity.CsFaq;
import com.shop.customerservice.service.CsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FAQ 知识库管理")
@RestController
@RequestMapping("/api/v1/cs/faqs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CsFaqController {

    private final CsService csService;

    @Operation(summary = "FAQ 分页列表")
    @GetMapping
    @PermitAll
    public Result<PageResult<CsFaq>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "20") Integer pageSize,
                                           @RequestParam(required = false) String category) {
        return Result.success(csService.getFaqPage(pageNum, pageSize, category));
    }

    @Operation(summary = "新增 FAQ")
    @PostMapping
    public Result<CsFaq> create(@Valid @RequestBody CsFaq faq) {
        return Result.success(csService.createFaq(faq));
    }

    @Operation(summary = "修改 FAQ")
    @PutMapping("/{id}")
    public Result<CsFaq> update(@PathVariable Long id, @Valid @RequestBody CsFaq faq) {
        return Result.success(csService.updateFaq(id, faq));
    }

    @Operation(summary = "删除 FAQ")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        csService.deleteFaq(id);
        return Result.success();
    }

    @Operation(summary = "批量导入 FAQ")
    @PostMapping("/batch-import")
    public Result<Integer> batchImport(@Valid @RequestBody FaqImportRequest request) {
        List<CsFaq> items = request.getItems().stream().map(item -> {
            CsFaq faq = new CsFaq();
            faq.setCategory(item.getCategory());
            faq.setQuestion(item.getQuestion());
            faq.setAnswer(item.getAnswer());
            faq.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0);
            faq.setStatus(1);
            return faq;
        }).toList();
        return Result.success(csService.batchImportFaq(items));
    }

    @Operation(summary = "导出全部 FAQ")
    @GetMapping("/export")
    @PermitAll
    public Result<List<CsFaq>> export() {
        return Result.success(csService.exportFaq());
    }
}
