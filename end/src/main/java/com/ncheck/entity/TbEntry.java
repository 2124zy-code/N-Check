package com.ncheck.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试题目/题解实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_entry")
public class TbEntry implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属租户用户ID
     */
    private Long userId;

    /**
     * 关联企业ID
     */
    private Long companyId;

    /**
     * 内容类型: 八股文 / 算法题
     */
    private String type;

    /**
     * 题目标题 / 核心考点
     */
    private String title;

    /**
     * 难度级别: 简单 / 中等 / 困难
     */
    private String difficulty;

    /**
     * 掌握熟练度: 未掌握 / 学习中 / 已掌握
     */
    private String status;

    /**
     * 是否高频必背星标: 0-否, 1-是
     */
    private Integer isStarred;

    /**
     * 标签 JSON 格式字符串 (如 ["Java基础", "集合框架"])
     */
    private String tags;

    /**
     * Markdown 题解内容 / 算法代码正文
     */
    private String content;

    /**
     * 最近复习时间
     */
    private LocalDateTime lastReviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
