package com.ncheck.security;

import com.ncheck.common.context.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 签发与解析工具 (JJWT 0.12.x)
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:ncheckSecretKeyForJwtAuthenticationTokens2026SecureKeyWithAtLeast32Bytes}")
    private String secret;

    @Value("${jwt.expiration:604800000}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(UserInfo user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("nickname", user.getNickname());
        claims.put("role", user.getRole());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token 获取 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取 UserInfo
     */
    public UserInfo getUserInfoFromToken(String token) {
        Claims claims = parseToken(token);
        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        String nickname = claims.get("nickname", String.class);
        String role = claims.get("role", String.class);

        return UserInfo.builder()
                .id(userId)
                .username(username)
                .nickname(nickname)
                .role(role)
                .build();
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
