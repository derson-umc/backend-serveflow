package com.serveflow.Service.User;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Fallback usado em dev/CI quando SMTP não está habilitado.
 * Apenas registra o token no log — NUNCA usar em produção.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailService implements EmailService {

    @Override
    public void sendPasswordResetEmail(String recipient, String username, String resetToken) {
        log.warn("[DEV] SMTP desabilitado. Token de reset para '{}': {}", username, resetToken);
    }
}
