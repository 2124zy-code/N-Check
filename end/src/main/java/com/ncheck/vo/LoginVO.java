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
@Schema(description = "登录成功响应视图")
public class LoginVO implements Serializable {

    @Schema(description = "JWT 访问凭证", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "用户信息")
    private UserVO user;
}
