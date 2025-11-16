package com.bookbuddy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:MyVerySecureJwtSecretKeyThatIsAtLeast32BytesLongForHS256Algorithm}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be provided");
        }
        
        byte[] keyBytes = decodeSecret(secret);
        
        // Ensure key is at least 32 bytes (256 bits) for HS256
        // If shorter, pad or repeat to meet minimum requirement
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            // If original key was shorter, repeat it to fill the rest
            for (int i = keyBytes.length; i < 32; i++) {
                paddedKey[i] = keyBytes[i % keyBytes.length];
            }
            keyBytes = paddedKey;
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    private byte[] decodeSecret(String rawSecret) {
        try {
            byte[] decoded = Base64.getDecoder().decode(rawSecret);
            // If Base64 decode succeeds but result is too short, use UTF-8 instead
            if (decoded.length < 32) {
                return rawSecret.getBytes(StandardCharsets.UTF_8);
            }
            return decoded;
        } catch (IllegalArgumentException ex) {
            // Not Base64, use as UTF-8
            return rawSecret.getBytes(StandardCharsets.UTF_8);
        }
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid JWT token", ex);
        }
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

