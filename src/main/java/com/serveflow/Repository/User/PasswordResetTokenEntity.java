package com.serveflow.Repository.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_prt_token_hash", columnList = "tokenHash", unique = true),
                @Index(name = "idx_prt_user_id", columnList = "userId")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;
}
