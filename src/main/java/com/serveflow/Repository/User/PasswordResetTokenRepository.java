package com.serveflow.Repository.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.serveflow.Model.User.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PasswordResetTokenRepository {

    private final SpringPasswordResetTokenRepository springRepo;

    @Transactional
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity;
        if (token.getId() == null) {
            entity = toEntity(token);
        } else {
            entity = springRepo.findById(token.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Token de reset inexistente: id=" + token.getId()));
            entity.setTokenHash(token.getTokenHash());
            entity.setExpiresAt(token.getExpiresAt());
            entity.setUsedAt(token.getUsedAt());
        }
        return toDomain(springRepo.save(entity));
    }

    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return springRepo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Transactional
    public void invalidateAllByUser(Long userId) {
        springRepo.invalidateAllByUser(userId, Instant.now());
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity e) {
        return new PasswordResetToken(
                e.getId(), e.getUserId(), e.getTokenHash(), e.getExpiresAt(), e.getUsedAt()
        );
    }

    private PasswordResetTokenEntity toEntity(PasswordResetToken t) {
        PasswordResetTokenEntity e = new PasswordResetTokenEntity();
        e.setUserId(t.getUserId());
        e.setTokenHash(t.getTokenHash());
        e.setExpiresAt(t.getExpiresAt());
        e.setUsedAt(t.getUsedAt());
        return e;
    }
}
