package com.itheim.program_platform_backend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：生成、解析、验证 Token
 */
public class JwtUtil {

    // 🔑 固定的 Base64 编码密钥（必须是 32 字节以上的原始数据）
    // 可通过命令生成：openssl rand -base64 32
    private static final String SECRET_KEY_BASE64 = "dGFuZ2RhaXhpbuaIkeecn+acjeS6hui/meS4quS7gOS5iOWvhumSpeS6hg=="; // 示例：44字符，32字节

    // ⏱ Token 过期时间：24小时
    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    // 🔐 解码后的密钥对象（静态初始化，确保唯一）
    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            Base64.getDecoder().decode(SECRET_KEY_BASE64)
    );

    /**
     * 生成 JWT Token
     *
     * @param claims 自定义载荷（如用户ID、角色等）
     * @return JWT 字符串
     */
    public static String generateToken(Map<String, Object> claims) {
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
    public static Claims parseToken(String token) throws JwtException {
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
    public static boolean isTokenExpired(String token) {
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