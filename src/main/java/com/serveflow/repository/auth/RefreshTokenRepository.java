package com.serveflow.repository.auth;

import com.serveflow.model.auth.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final SpringRefreshTokenRepository springRepository;

    @Transactional
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUserId(token.userId());
        entity.setTokenHash(token.tokenHash());
        entity.setExpiresAt(token.expiresAt());
        return toDomain(springRepository.save(entity));
    }

    public Optional<RefreshToken> findByTokenHash(String hash) {
        return springRepository.findByTokenHash(hash).map(this::toDomain);
    }

    @Transactional
    public void deleteById(Long id) {
        springRepository.deleteById(id);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        springRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteExpiredTokens() {
        springRepository.deleteExpiredTokens(Instant.now());
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return new RefreshToken(entity.getId(), entity.getUserId(), entity.getTokenHash(), entity.getExpiresAt());
    }
}
