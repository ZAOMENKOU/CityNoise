package com.example.city_noise_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT Token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    private SecretKey secretKey;

    /**
     * 初始化，将Base64编码的密钥解码
     */
    @PostConstruct
    public void init() {
        // 将Base64编码的密钥解码为字节数组并创建SecretKey对象
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT Token
     * 
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return 生成的Token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        // 创建Token的声明（payload）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        // 当前时间
        Date now = new Date();
        // Token过期时间
        Date expireDate = new Date(now.getTime() + expire * 1000);

        // 生成Token
        return Jwts.builder()
                .setClaims(claims) // 设置声明
                .setIssuedAt(now) // 设置签发时间
                .setExpiration(expireDate) // 设置过期时间
                .signWith(secretKey) // 使用密钥签名（自动选择算法）
                .compact();
    }

    /**
     * 从Token中解析Claims（声明）
     * 
     * @param token JWT Token
     * @return Claims对象，如果解析失败返回null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 设置签名密钥
                    .build()
                    .parseClaimsJws(token) // 解析Token
                    .getBody(); // 获取声明
        } catch (Exception e) {
            // Token解析失败（可能是格式错误、签名无效、已过期等）
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID，如果解析失败返回null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名，如果解析失败返回null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从Token中获取用户角色
     * 
     * @param token JWT Token
     * @return 用户角色，如果解析失败返回null
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return 如果Token有效且未过期返回true，否则返回false
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        // 检查Token是否过期
        return !claims.getExpiration().before(new Date());
    }

    /**
     * 获取Token的剩余有效时间（秒）
     * 
     * @param token JWT Token
     * @return 剩余有效时间（秒），如果Token无效返回0
     */
    public Long getRemainingTime(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return 0L;
        }

        Date expiration = claims.getExpiration();
        Date now = new Date();

        long remainingMillis = expiration.getTime() - now.getTime();
        return remainingMillis > 0 ? remainingMillis / 1000 : 0;
    }
}