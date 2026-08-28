package com.ncheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "每日复盘随笔保存参数")
public class DailyNoteDTO {

    @NotBlank(message = "复盘日期不能为空 (YYYY-MM-DD)")
    @Schema(description = "复盘日期", example = "2026-08-28", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewDate;

    @Schema(description = "复盘随笔正文", example = "今天攻克了滑动窗口与 Redis 分布式锁，状态很好！")
    private String noteContent;
}
