package com.ncheck.controller;

import com.ncheck.common.result.Result;
import com.ncheck.dto.CompanyDTO;
import com.ncheck.service.CompanyService;
import com.ncheck.vo.CompanyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "2. 目标企业档案模块")
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "获取当前用户企业列表", description = "返回所有企业并包含各自八股数、算法数与掌握数统计")
    @GetMapping
    public Result<List<CompanyVO>> getCompanyList(
            @Parameter(description = "企业名称/行业搜索关键字") @RequestParam(required = false) String keyword
    ) {
        return Result.success(companyService.getCompanyList(keyword));
    }

    @Operation(summary = "获取单家企业详情")
    @GetMapping("/{id}")
    public Result<CompanyVO> getCompanyById(@PathVariable("id") Long id) {
        return Result.success(companyService.getCompanyById(id));
    }

    @Operation(summary = "创建目标名企档案")
    @PostMapping
    public Result<CompanyVO> createCompany(@Valid @RequestBody CompanyDTO dto) {
        return Result.success("企业建档成功", companyService.createCompany(dto));
    }

    @Operation(summary = "编辑修改企业信息")
    @PutMapping("/{id}")
    public Result<CompanyVO> updateCompany(@PathVariable("id") Long id, @Valid @RequestBody CompanyDTO dto) {
        return Result.success("企业信息更新成功", companyService.updateCompany(id, dto));
    }

    @Operation(summary = "删除企业", description = "删除企业并将级联清理所属的题目")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCompany(@PathVariable("id") Long id) {
        companyService.deleteCompany(id);
        return Result.success("企业已成功删除", null);
    }
}
