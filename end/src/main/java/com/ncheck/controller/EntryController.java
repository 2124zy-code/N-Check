package com.ncheck.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ncheck.common.result.Result;
import com.ncheck.dto.EntryDTO;
import com.ncheck.dto.EntryQueryDTO;
import com.ncheck.dto.MockDrawDTO;
import com.ncheck.dto.UpdateStatusDTO;
import com.ncheck.service.EntryService;
import com.ncheck.vo.EntryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "3. 面经题库与题解模块")
@RestController
@RequestMapping("/api/v1/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @Operation(summary = "多条件动态分页检索题目", description = "支持企业、题型、难度、熟练度、星标、关键字及标签多维过滤")
    @GetMapping
    public Result<IPage<EntryVO>> getEntryPage(EntryQueryDTO query) {
        return Result.success(entryService.getEntryPage(query));
    }

    @Operation(summary = "获取题目详情")
    @GetMapping("/{id}")
    public Result<EntryVO> getEntryById(@PathVariable("id") Long id) {
        return Result.success(entryService.getEntryById(id));
    }

    @Operation(summary = "录入新面试题")
    @PostMapping
    public Result<EntryVO> createEntry(@Valid @RequestBody EntryDTO dto) {
        return Result.success("题目录入成功", entryService.createEntry(dto));
    }

    @Operation(summary = "编辑修改面试题")
    @PutMapping("/{id}")
    public Result<EntryVO> updateEntry(@PathVariable("id") Long id, @Valid @RequestBody EntryDTO dto) {
        return Result.success("题目更新成功", entryService.updateEntry(id, dto));
    }

    @Operation(summary = "删除面试题")
    @DeleteMapping("/{id}")
    public Result<Void> deleteEntry(@PathVariable("id") Long id) {
        entryService.deleteEntry(id);
        return Result.success("题目已删除", null);
    }

    @Operation(summary = "快速切换高频星标状态")
    @PatchMapping("/{id}/star")
    public Result<EntryVO> toggleStar(@PathVariable("id") Long id) {
        return Result.success("星标状态已更新", entryService.toggleStar(id));
    }

    @Operation(summary = "快速更新掌握熟练度状态 (未掌握/学习中/已掌握)")
    @PatchMapping("/{id}/status")
    public Result<EntryVO> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody UpdateStatusDTO dto) {
        return Result.success("掌握熟练度已更新", entryService.updateMasteryStatus(id, dto.getStatus()));
    }

    @Operation(summary = "模拟面试智能抽题", description = "按指定公司与题目类型加权随机抽取 N 道八股 + M 道手撕")
    @PostMapping("/mock-draw")
    public Result<List<EntryVO>> drawMockInterview(@RequestBody MockDrawDTO dto) {
        return Result.success(entryService.drawMockInterview(dto));
    }
}
