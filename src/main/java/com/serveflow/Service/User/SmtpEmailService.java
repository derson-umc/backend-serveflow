package com.serveflow.Service.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.reset-link-base-url:http://localhost:5173/reset-password}")
    private String resetLinkBaseUrl;

    @Override
    public void sendPasswordResetEmail(String recipient, String username, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("[ServeFlow] Recuperação de senha");
        message.setText(buildBody(username, resetToken));
        mailSender.send(message);
        log.info("E-mail de reset enviado para usuário '{}'", username);
    }

    private String buildBody(String username, String token) {
        String link = resetLinkBaseUrl + "?token=" + token;
        return """
                Olá %s,

                Recebemos uma solicitação para redefinir sua senha.
                Para continuar, acesse o link abaixo (válido por 15 minutos):

                %s

                Se você não solicitou esta redefinição, ignore este e-mail.
                Por segurança, nunca compartilhe este link.

                Equipe ServeFlow
                """.formatted(username, link);
    }
}
