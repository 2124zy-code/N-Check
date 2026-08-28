package com.ncheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "题目多条件分页查询参数")
public class EntryQueryDTO {

    @Schema(description = "页码 (默认1)", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小 (默认20)", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "筛选企业ID", example = "1")
    private Long companyId;

    @Schema(description = "筛选内容类型: 八股文 / 算法题", example = "八股文")
    private String type;

    @Schema(description = "筛选难度: 简单 / 中等 / 困难", example = "中等")
    private String difficulty;

    @Schema(description = "筛选熟练度: 未掌握 / 学习中 / 已掌握", example = "已掌握")
    private String status;

    @Schema(description = "筛选是否星标 (0或1)", example = "1")
    private Integer isStarred;

    @Schema(description = "标题/内容全文搜索关键字", example = "HashMap")
    private String keyword;

    @Schema(description = "指定标签过滤", example = "Java基础")
    private String tag;
}
