package com.example.notification_system.security;

import java.util.Date;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtTokenUtil {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private JwtDecoder jwtDecoder;

    @PostConstruct
    public void init() {
    
        this.jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Claims claims = Jwts.claims();
            claims.putAll(jwt.getClaims()); 
            return claims;
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    public Boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token); 
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
