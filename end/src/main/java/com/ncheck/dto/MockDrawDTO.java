package com.ncheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "智能抽题模拟面试请求参数")
public class MockDrawDTO {

    @Schema(description = "限定目标企业ID (留空则全库抽取)", example = "1")
    private Long companyId;

    @Schema(description = "八股文抽取题数 (默认3题)", example = "3")
    private Integer baguCount = 3;

    @Schema(description = "算法手撕抽取题数 (默认1题)", example = "1")
    private Integer algoCount = 1;
}
