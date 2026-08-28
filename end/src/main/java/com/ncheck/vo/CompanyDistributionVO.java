package com.ncheck.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业题库分布统计视图")
public class CompanyDistributionVO implements Serializable {

    @Schema(description = "企业ID", example = "1")
    private Long id;

    @Schema(description = "企业名称", example = "字节跳动")
    private String name;

    @Schema(description = "收录题数", example = "18")
    private Long count;

    @Schema(description = "占比百分比 (0-100)", example = "35")
    private Integer percentage;

    @Schema(description = "图表推荐配色", example = "#2563eb")
    private String color;
}
