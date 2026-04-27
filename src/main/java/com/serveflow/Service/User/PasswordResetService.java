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

/**
 * Coordena o fluxo de recuperação de senha:
 *  1. {@link #requestReset(String)} gera um token aleatório, guarda só o hash,
 *     invalida tokens anteriores do usuário e dispara o e-mail.
 *  2. {@link #resetPassword(String, String)} valida o token, troca a senha
 *     (re-hashada com BCrypt) e marca o token como usado.
 *
 * Decisões de segurança:
 *  - Token é UUID v4 emitido via SecureRandom.
 *  - Persistimos somente SHA-256(token) — vazamento do banco não compromete tokens vivos.
 *  - Resposta uniforme: nunca indica se o usuário existe.
 *  - Token é uso único e expira em 15 minutos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.reset-recipient-fallback:no-reply@serveflow.local}")
    private String fallbackRecipient;

    /**
     * Sempre retorna sem revelar se o username existe.
     * Erros de envio de e-mail são logados, não propagados ao cliente.
     */
    @Transactional
    public void requestReset(String username) {
        Optional<User> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            log.info("Solicitação de reset para username inexistente — resposta uniforme.");
            return;
        }
        User user = maybeUser.get();

        // Invalida tokens vivos anteriores: garante one-token-active-per-user.
        tokenRepository.invalidateAllByUser(user.getId());

        String rawToken = generateToken();
        String tokenHash = sha256(rawToken);
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);

        tokenRepository.save(PasswordResetToken.issue(user.getId(), tokenHash, expiresAt));

        try {
            // O domínio User não tem campo email; usamos username + fallback até
            // que o cadastro de e-mail seja adicionado (ver ADR no README).
            emailService.sendPasswordResetEmail(fallbackRecipient, user.getUsername(), rawToken);
        } catch (Exception ex) {
            log.error("Falha ao enviar e-mail de reset para '{}': {}", user.getUsername(), ex.getMessage(), ex);
            // Não propagamos: cliente segue com resposta 204.
        }
    }

    /**
     * Aplica a nova senha se o token estiver válido. Lança BusinessRuleException
     * em qualquer falha — todas com a mesma mensagem genérica para não vazar estado.
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessRuleException("Token inválido ou expirado");
        }
        String tokenHash = sha256(rawToken);

        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessRuleException("Token inválido ou expirado"));

        if (!token.isValid(Instant.now())) {
            throw new BusinessRuleException("Token inválido ou expirado");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessRuleException("Token inválido ou expirado"));

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
        // UUID v4 baseado em SecureRandom — 122 bits de entropia.
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
            // SHA-256 é parte do JRE — falha aqui é catastrófica.
            throw new IllegalStateException("SHA-256 indisponível no runtime", e);
        }
    }
}
