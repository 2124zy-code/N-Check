package com.ncheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "企业档案保存/更新参数")
public class CompanyDTO {

    @Schema(description = "企业标识代号 (留空自动生成)", example = "bytedance")
    private String companyCode;

    @NotBlank(message = "企业名称不能为空")
    @Schema(description = "企业名称", example = "字节跳动", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Logo代号", example = "bytedance")
    private String logo;

    @Schema(description = "所属行业", example = "互联网/短视频/云计算")
    private String industry;
}
