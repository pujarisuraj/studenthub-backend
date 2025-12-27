package com.college.campuscollab.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

        private static final String SECRET = "campus-collab-secret-key-256-bit-secure";

        private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours

        private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        public String generateToken(String username) {
                return Jwts.builder()
                                .setSubject(username)
                                .setIssuedAt(new Date())
                                .setExpiration(
                                                new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                                .signWith(secretKey)
                                .compact();
        }

        public String extractUsername(String token) {
                return Jwts.parserBuilder()
                                .setSigningKey(secretKey)
                                .build()
                                .parseClaimsJws(token)
                                .getBody()
                                .getSubject();
        }

        public Date extractExpiration(String token) {
                return Jwts.parserBuilder()
                                .setSigningKey(secretKey)
                                .build()
                                .parseClaimsJws(token)
                                .getBody()
                                .getExpiration();
        }

        public boolean isTokenExpired(String token) {
                try {
                        return extractExpiration(token).before(new Date());
                } catch (Exception e) {
                        return true; // If extraction fails, consider token expired
                }
        }

        public boolean validateToken(String token,
                        org.springframework.security.core.userdetails.UserDetails userDetails) {
                try {
                        String username = extractUsername(token);
                        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
                } catch (Exception e) {
                        return false;
                }
        }
}
