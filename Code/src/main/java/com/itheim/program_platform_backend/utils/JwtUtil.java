package com.itheim.program_platform_backend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：生成、解析、验证 Token
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private static SecretKey KEY;
    private static long EXPIRATION;

    @PostConstruct
    public void init() {
        KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        EXPIRATION = expiration;
        log.info("JWT 工具类初始化完成，过期时间: {} 毫秒", EXPIRATION);
    }

    /**
     * 生成 JWT Token
     *
     * @param claims 自定义载荷（如用户ID、角色等）
     * @return JWT 字符串
     */
    public String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT Token
     *
     * @param token JWT 字符串
     * @return Claims 载荷
     * @throws JwtException 解析失败时抛出
     */
    public Claims parseToken(String token) throws JwtException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token 已过期", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("Token 格式错误", e);
        } catch (SignatureException e) {
            throw new JwtException("Token 签名验证失败", e);
        } catch (Exception e) {
            throw new JwtException("无效的 Token", e);
        }
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT 字符串
     * @return true: 已过期或无效，false: 未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            parseToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }

    // 自定义异常类
    public static class JwtException extends Exception {
        public JwtException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}