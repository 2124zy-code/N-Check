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
@Schema(description = "六维技术雷达单个维度统计视图")
public class RadarDimensionVO implements Serializable {

    @Schema(description = "维度标识", example = "java_jvm")
    private String key;

    @Schema(description = "维度名称", example = "Java核心与JVM")
    private String dimension;

    @Schema(description = "该维度收录总题数", example = "10")
    private Long count;

    @Schema(description = "该维度已掌握题数", example = "8")
    private Long masteredCount;

    @Schema(description = "雷达自适应得分 (0-100)", example = "85")
    private Integer score;
}
