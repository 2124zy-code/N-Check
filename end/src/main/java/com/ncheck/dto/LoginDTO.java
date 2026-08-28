package com.ncheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户登录请求参数")
public class LoginDTO {

    @NotBlank(message = "账号名不能为空")
    @Schema(description = "登录账号名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "登录密码不能为空")
    @Schema(description = "登录密码", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
