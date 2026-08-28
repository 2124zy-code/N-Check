package com.ncheck.controller;

import com.ncheck.common.result.Result;
import com.ncheck.dto.LoginDTO;
import com.ncheck.dto.RegisterDTO;
import com.ncheck.service.AuthService;
import com.ncheck.vo.LoginVO;
import com.ncheck.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1. 用户与鉴权认证模块")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户账号登录", description = "校验账号密码并签发 JWT Bearer Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("登录成功", authService.login(dto));
    }

    @Operation(summary = "新用户注册", description = "注册独立账号并自动分配初始 6 家名企与示范题库")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success("注册成功", authService.register(dto));
    }

    @Operation(summary = "获取当前登录用户信息", description = "需在请求头携带 Authorization: Bearer <token>")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }
}
