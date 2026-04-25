package com.serveflow.Service.PasswordReset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.serveflow.Exception.User.BusinessRuleException;
import com.serveflow.Exception.User.InvalidTokenException;
import com.serveflow.Exception.User.UserNotFoundException;
import com.serveflow.Model.PasswordReset.PasswordResetToken;
import com.serveflow.Model.User.User;
import com.serveflow.Repository.PasswordReset.PasswordResetTokenRepository;
import com.serveflow.Repository.User.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder encoder;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Gera token de reset e simula envio. Não revela se o usuário existe.
     */
    @Transactional
    public void requestReset(String username) {
        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    tokenRepository.invalidateActiveTokensForUser(user.getId());
                    PasswordResetToken issued = tokenRepository.save(PasswordResetToken.issueFor(user.getId()));
                    String link = frontendUrl + "/reset-password?token=" + issued.getToken();
                    log.info("[PasswordReset] Link gerado para '{}' (expira em {} min): {}",
                            username, PasswordResetToken.TTL_MINUTES, link);
                },
                () -> log.info("[PasswordReset] Solicitação para username inexistente: '{}'", username)
        );
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Token de redefinição inválido"));

        if (!token.isValid()) {
            throw new InvalidTokenException("Token de redefinição expirado ou já utilizado");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UserNotFoundException(token.getUserId()));

        User updated = new User(user.getId(), user.getUsername(), encoder.encode(newPassword), user.getRole());
        userRepository.save(updated);
        tokenRepository.save(token.markUsed());
        log.info("[PasswordReset] Senha redefinida para userId={}", user.getId());
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessRuleException("Senha atual incorreta");
        }
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new BusinessRuleException("A nova senha deve ser diferente da atual");
        }

        User updated = new User(user.getId(), user.getUsername(), encoder.encode(newPassword), user.getRole());
        userRepository.save(updated);
        log.info("[PasswordReset] Senha alterada por '{}'", username);
    }
}
