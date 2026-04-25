package com.serveflow.Repository.PasswordReset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.serveflow.Model.PasswordReset.PasswordResetToken;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PasswordResetTokenRepository {

    private final SpringPasswordResetTokenRepository springRepository;

    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity;
        if (token.getId() == null) {
            entity = new PasswordResetTokenEntity();
        } else {
            entity = springRepository.findById(token.getId()).orElseGet(PasswordResetTokenEntity::new);
        }
        entity.setUserId(token.getUserId());
        entity.setToken(token.getToken());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsed(token.isUsed());
        return toDomain(springRepository.save(entity));
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        return springRepository.findByToken(token).map(this::toDomain);
    }

    @Transactional
    public void invalidateActiveTokensForUser(Long userId) {
        springRepository.invalidateActiveTokensForUser(userId);
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                entity.getId(),
                entity.getUserId(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.isUsed()
        );
    }
}
