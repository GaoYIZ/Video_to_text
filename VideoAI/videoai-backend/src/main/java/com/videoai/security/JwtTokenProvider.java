package com.videoai.security;

import com.videoai.config.JwtProperties;
import com.videoai.model.vo.LoginUserVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(LoginUserVO loginUser) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(loginUser.getUserId()))
                .claim("userNo", loginUser.getUserNo())
                .claim("username", loginUser.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getExpireSeconds())))
                .signWith(secretKey)
                .compact();
    }

    public LoginUserVO parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return LoginUserVO.builder()
                .userId(Long.valueOf(claims.getSubject()))
                .userNo(claims.get("userNo", String.class))
                .username(claims.get("username", String.class))
                .build();
    }

    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}
