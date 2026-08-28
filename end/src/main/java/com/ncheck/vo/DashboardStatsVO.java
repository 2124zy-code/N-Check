package com.ncheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工作台总览统计大盘视图")
public class DashboardStatsVO implements Serializable {

    @Schema(description = "题库总收录数", example = "60")
    private Long totalEntries;

    @Schema(description = "已熟练掌握题目数", example = "42")
    private Long masteredEntries;

    @Schema(description = "学习中题目数", example = "12")
    private Long learningEntries;

    @Schema(description = "未掌握题目数", example = "6")
    private Long unmasteredEntries;

    @Schema(description = "高频必背星标数", example = "18")
    private Long starredEntries;

    @Schema(description = "目标名企总数", example = "6")
    private Long totalCompanies;

    @Schema(description = "总掌握率百分比 (0-100)", example = "70")
    private Integer masteryRate;

    @Schema(description = "六维技术雷达图数据")
    private List<RadarDimensionVO> radarStats;

    @Schema(description = "名企收纳分布数据")
    private List<CompanyDistributionVO> companyDistribution;
}
