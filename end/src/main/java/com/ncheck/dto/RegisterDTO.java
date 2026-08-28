package com.ncheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户注册请求参数")
public class RegisterDTO {

    @NotBlank(message = "账号名不能为空")
    @Size(min = 2, max = 32, message = "账号长度需在 2 到 32 个字符之间")
    @Schema(description = "注册用户名", example = "developer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 4, max = 32, message = "密码长度需在 4 到 32 个字符之间")
    @Schema(description = "登录密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "用户昵称 (留空默认使用用户名)", example = "技术面霸")
    private String nickname;
}
