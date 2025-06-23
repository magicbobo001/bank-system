package com.bank.customer.security;

import com.bank.customer.entity.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

import javax.crypto.SecretKey;

@Service
public class JwtTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final String SECRET_KEY_STRING = "a-very-secure-key-with-exactly-64-bytes-1234567890abcdefghijklmnopqrstuvwxyz";
    private final SecretKey secretKey; // 替换为 SecretKey 类型

    public JwtTokenProvider() {
        // 将字符串密钥转换为 SecretKey（仅示例，生产环境应避免硬编码）
        this.secretKey = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    }

    private static final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS512) // 替换为 SecretKey 签名方法
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 替换为 SecretKey 设置签名密钥
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
