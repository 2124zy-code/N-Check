package com.ncheck.controller;

import com.ncheck.common.result.Result;
import com.ncheck.dto.DailyNoteDTO;
import com.ncheck.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "4. 每日复盘与日记模块")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "查询指定日期的复盘详情", description = "获取指定日期的随笔与该日攻克的题目列表")
    @GetMapping("/daily")
    public Result<Map<String, Object>> getDailyReview(
            @Parameter(description = "复盘日期 (格式: YYYY-MM-DD)", example = "2026-08-28")
            @RequestParam String date
    ) {
        return Result.success(reviewService.getDailyReview(date));
    }

    @Operation(summary = "保存/更新每日复盘随笔")
    @PostMapping("/daily")
    public Result<String> saveDailyNote(@Valid @RequestBody DailyNoteDTO dto) {
        String saved = reviewService.saveDailyNote(dto);
        return Result.success("复盘随笔保存成功", saved);
    }
}
