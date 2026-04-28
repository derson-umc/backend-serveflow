package com.serveflow.Service.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.serveflow.Exception.User.BusinessRuleException;
import com.serveflow.Model.User.PasswordResetToken;
import com.serveflow.Model.User.User;
import com.serveflow.Repository.User.PasswordResetTokenRepository;
import com.serveflow.Repository.User.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final String INVALID_TOKEN_MESSAGE = "Token inválido ou expirado";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.reset-recipient-fallback:no-reply@serveflow.local}")
    private String fallbackRecipient;

    @Transactional
    public void requestReset(String username) {
        Optional<User> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            return;
        }
        User user = maybeUser.get();

        tokenRepository.invalidateAllByUser(user.getId());

        String rawToken = generateToken();
        String tokenHash = sha256(rawToken);
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);

        tokenRepository.save(PasswordResetToken.issue(user.getId(), tokenHash, expiresAt));

        try {
            emailService.sendPasswordResetEmail(fallbackRecipient, user.getUsername(), rawToken);
        } catch (Exception ex) {
            log.error("Falha ao enviar e-mail de reset para '{}': {}", user.getUsername(), ex.getMessage(), ex);
        }
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessRuleException(INVALID_TOKEN_MESSAGE);
        }
        String tokenHash = sha256(rawToken);

        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessRuleException(INVALID_TOKEN_MESSAGE));

        if (!token.isValid(Instant.now())) {
            throw new BusinessRuleException(INVALID_TOKEN_MESSAGE);
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessRuleException(INVALID_TOKEN_MESSAGE));

        User updated = new User(
                user.getId(),
                user.getUsername(),
                passwordEncoder.encode(newPassword),
                user.getRole(),
                user.getJobposition()
        );
        userRepository.save(updated);

        tokenRepository.save(token.markUsed(Instant.now()));
        log.info("Senha redefinida com sucesso para usuário id={}", user.getId());
    }

    private String generateToken() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return UUID.nameUUIDFromBytes(bytes).toString().replace("-", "")
                + Long.toHexString(secureRandom.nextLong());
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível no runtime", e);
        }
    }
}
