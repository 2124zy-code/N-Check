package com.ncheck.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "未登录或登录态已失效，请重新登录"),
    FORBIDDEN(403, "无权限访问该资源"),
    NOT_FOUND(404, "请求的资源不存在"),
    USER_EXISTS(4001, "该账号已存在，请更换其他用户名"),
    USER_NOT_FOUND(4002, "账号或密码错误"),
    PASSWORD_ERROR(4003, "账号或密码错误"),
    COMPANY_NOT_FOUND(4004, "目标企业不存在"),
    ENTRY_NOT_FOUND(4005, "题目记录不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部异常，请稍后重试");

    private final int code;
    private final String message;
}
