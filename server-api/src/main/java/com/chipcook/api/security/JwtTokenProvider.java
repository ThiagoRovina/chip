package com.chipcook.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:chipcook-secret-key-muito-longa-para-seguranca-256bits-minimum}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(UUID usuarioId, String email, String tenantId, String nomeUsuario) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("email", email)
                .claim("tenantId", tenantId)
                .claim("nomeUsuario", nomeUsuario)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Erro ao validar JWT: ", e);
            return null;
        }
    }

    public String getTenantIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get("tenantId", String.class);
    }

    public String getUsuarioIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.getSubject();
    }

    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) return null;
        return claims.get("email", String.class);
    }

    public boolean isTokenValid(String token) {
        return validateToken(token) != null;
    }
}

