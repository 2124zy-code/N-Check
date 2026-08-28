package com.ncheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "面试题录入/编辑参数")
public class EntryDTO {

    @NotNull(message = "关联企业ID不能为空")
    @Schema(description = "目标企业ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long companyId;

    @NotBlank(message = "内容类型不能为空 (八股文/算法题)")
    @Schema(description = "内容类型: 八股文 / 算法题", example = "八股文", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotBlank(message = "题目标题不能为空")
    @Schema(description = "题目标题 / 核心考点", example = "Java HashMap 扩容机制及 1.7 与 1.8 差异", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "难度级别: 简单 / 中等 / 困难", example = "中等")
    private String difficulty;

    @Schema(description = "熟练度: 未掌握 / 学习中 / 已掌握", example = "未掌握")
    private String status;

    @Schema(description = "是否星标高频: 0-否, 1-是", example = "1")
    private Integer isStarred;

    @Schema(description = "标签分类列表", example = "[\"Java基础\", \"集合框架\"]")
    private List<String> tags;

    @Schema(description = "Markdown题解 / 算法手撕代码", example = "### 原理解析\n...")
    private String content;
}
