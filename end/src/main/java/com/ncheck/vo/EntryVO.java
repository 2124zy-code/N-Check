package com.ncheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "面试题目详情视图")
public class EntryVO implements Serializable {

    @Schema(description = "题目ID", example = "1")
    private Long id;

    @Schema(description = "所属用户ID", example = "1")
    private Long userId;

    @Schema(description = "关联企业ID", example = "1")
    private Long companyId;

    @Schema(description = "关联企业名称", example = "字节跳动")
    private String companyName;

    @Schema(description = "企业Logo代号", example = "bytedance")
    private String companyLogo;

    @Schema(description = "内容类型: 八股文 / 算法题", example = "八股文")
    private String type;

    @Schema(description = "题目标题 / 核心考点", example = "Java HashMap 扩容机制及 1.7 与 1.8 差异")
    private String title;

    @Schema(description = "难度: 简单 / 中等 / 困难", example = "中等")
    private String difficulty;

    @Schema(description = "掌握熟练度: 未掌握 / 学习中 / 已掌握", example = "已掌握")
    private String status;

    @Schema(description = "是否星标高频: 0-否, 1-是", example = "1")
    private Integer isStarred;

    @Schema(description = "标签列表", example = "[\"Java基础\", \"集合框架\"]")
    private List<String> tags;

    @Schema(description = "Markdown题解 / 算法代码正文")
    private String content;

    @Schema(description = "最近复习时间")
    private LocalDateTime lastReviewedAt;

    @Schema(description = "录入时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
