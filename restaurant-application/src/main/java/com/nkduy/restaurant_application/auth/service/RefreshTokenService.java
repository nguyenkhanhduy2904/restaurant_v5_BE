package com.nkduy.restaurant_application.auth.service;

import com.nkduy.restaurant_application.auth.entity.RefreshToken;
import com.nkduy.restaurant_application.auth.repository.RefreshTokenRepository;
import com.nkduy.restaurant_application.user.entity.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service

public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.repository = repository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /** Saves the hash, returns the raw token to give to the client. */
    @Transactional
    public String issue(User user) {

       String raw = UUID.randomUUID().toString();

        Instant now = Instant.now();
        repository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(DigestUtils.sha256Hex(raw))
                .createdAt(now)
                .expiresAt(now.plusMillis(refreshExpirationMs))
                .build());
        return raw;
    }

    @Transactional
    public User validateAndConsume(String refreshToken) {
        String hash = DigestUtils.sha256Hex(refreshToken);

        RefreshToken token = repository.findByTokenHash(hash).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if(token.getExpiresAt().isBefore(Instant.now()) || token.getRevokedAt() != null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }
        token.setRevokedAt(Instant.now());
        repository.save(token);

        return token.getUser();
    }

    @Transactional
    public void revokeIfPresent(String refreshToken) {
        String hash = DigestUtils.sha256Hex(refreshToken);

        repository.findByTokenHash(hash).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
            }
        });
    }





}
