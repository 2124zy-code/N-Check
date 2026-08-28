package com.ncheck.controller;

import com.ncheck.common.result.Result;
import com.ncheck.service.StatsService;
import com.ncheck.vo.CompanyDistributionVO;
import com.ncheck.vo.DashboardStatsVO;
import com.ncheck.vo.RadarDimensionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "5. 数据大盘与统计下沉模块")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "获取工作台总览大盘统计", description = "包含题目总数、已掌握数、掌握率、六维雷达图与企业收纳分布")
    @GetMapping("/dashboard")
    public Result<DashboardStatsVO> getDashboardStats() {
        return Result.success(statsService.getDashboardStats());
    }

    @Operation(summary = "获取六维技术雷达图", description = "计算机基础、JVM、JUC、MySQL、Spring中间件与算法手撕 6 个维度的聚合打分")
    @GetMapping("/radar")
    public Result<List<RadarDimensionVO>> getRadarStats() {
        return Result.success(statsService.getRadarStats());
    }

    @Operation(summary = "获取名企题库收纳占比分布")
    @GetMapping("/distribution")
    public Result<List<CompanyDistributionVO>> getCompanyDistribution() {
        return Result.success(statsService.getCompanyDistribution());
    }
}
