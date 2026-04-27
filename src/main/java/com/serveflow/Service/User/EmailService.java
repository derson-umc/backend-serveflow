package com.serveflow.Service.User;

/**
 * Abstração para envio de e-mails transacionais. Mantém o domínio
 * desacoplado do mecanismo concreto (SMTP, SES, etc.).
 */
public interface EmailService {

    void sendPasswordResetEmail(String recipient, String username, String resetToken);
}
