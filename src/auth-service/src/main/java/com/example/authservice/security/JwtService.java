package com.example.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final Key key;
    private final long ttlMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.ttl-ms}") long ttlMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.ttlMs = ttlMs;
    }

    public String generate(Long userId, String username, String rolesCsv) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))             // we'll read this as principal
                .addClaims(Map.of("username", username, "roles", rolesCsv))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(ttlMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
