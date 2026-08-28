package com.ncheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "熟练度变更参数")
public class UpdateStatusDTO {

    @NotBlank(message = "状态值不能为空 (未掌握 / 学习中 / 已掌握)")
    @Schema(description = "新状态值", example = "已掌握", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
}
