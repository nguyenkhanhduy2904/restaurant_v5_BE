package com.nkduy.restaurant_application.auth.service;

import com.nkduy.restaurant_application.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;


    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }


    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getId())          // who the token is for
                .claim("role", user.getRole())      // extra data
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)                      // signs with HMAC-SHA
                .compact();                         // builds the final string
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)           // checks signature and expiry
                .getPayload();
    }

    public String extractUserId(String token) {
        return parse(token).getSubject();
    }



}
