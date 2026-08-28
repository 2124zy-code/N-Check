package com.ncheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业档案视图")
public class CompanyVO implements Serializable {

    @Schema(description = "企业主键ID", example = "1")
    private Long id;

    @Schema(description = "所属用户ID", example = "1")
    private Long userId;

    @Schema(description = "企业标识代号", example = "bytedance")
    private String companyCode;

    @Schema(description = "企业名称", example = "字节跳动")
    private String name;

    @Schema(description = "Logo代号", example = "bytedance")
    private String logo;

    @Schema(description = "所属行业", example = "互联网/短视频/云计算")
    private String industry;

    @Schema(description = "八股文题数统计", example = "15")
    private Long baguCount;

    @Schema(description = "算法手撕题数统计", example = "5")
    private Long algoCount;

    @Schema(description = "已熟练掌握题数统计", example = "12")
    private Long masteredCount;

    @Schema(description = "总收纳题数统计", example = "20")
    private Long totalCount;

    @Schema(description = "建档时间")
    private LocalDateTime createTime;
}
