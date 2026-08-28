package com.ncheck.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncheck.common.context.UserContext;
import com.ncheck.common.context.UserInfo;
import com.ncheck.common.result.Result;
import com.ncheck.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行 OPTIONS 跨域预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 2. 从 Header 中获取 Authorization
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "未提供身份凭据，请登录");
            return false;
        }

        String token = authHeader.substring(7);

        // 3. 校验 Token
        if (!jwtUtil.validateToken(token)) {
            sendUnauthorizedResponse(response, "登录凭证已过期或无效，请重新登录");
            return false;
        }

        // 4. 解析并设置当前用户上下文
        try {
            UserInfo userInfo = jwtUtil.getUserInfoFromToken(token);
            UserContext.setUser(userInfo);
            return true;
        } catch (Exception e) {
            log.error("Token 解析失败: {}", e.getMessage());
            sendUnauthorizedResponse(response, "登录态解析异常，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束务必清理 ThreadLocal，防止线程池复用导致内存泄漏和数据污染
        UserContext.clear();
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
