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
 * 目标企业实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_company")
public class TbCompany implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属租户用户ID
     */
    private Long userId;

    /**
     * 企业标识代号 (如 bytedance)
     */
    private String companyCode;

    /**
     * 企业名称 (如 字节跳动)
     */
    private String name;

    /**
     * Logo代号
     */
    private String logo;

    /**
     * 所属行业
     */
    private String industry;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
