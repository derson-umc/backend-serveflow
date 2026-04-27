package com.serveflow.Repository.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SpringPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update PasswordResetTokenEntity t set t.usedAt = :now " +
            "where t.userId = :userId and t.usedAt is null")
    int invalidateAllByUser(@Param("userId") Long userId, @Param("now") Instant now);
}
