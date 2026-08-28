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
@Schema(description = "用户信息响应视图")
public class UserVO implements Serializable {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "登录账号", example = "admin")
    private String username;

    @Schema(description = "用户昵称", example = "管理员")
    private String nickname;

    @Schema(description = "头像配色", example = "#4f46e5")
    private String avatarColor;

    @Schema(description = "角色", example = "ADMIN")
    private String role;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;
}
