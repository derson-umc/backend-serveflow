package com.serveflow.service.auth;

import com.serveflow.exception.auth.InvalidResetTokenException;
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.repository.auth.PasswordResetTokenEntity;
import com.serveflow.repository.auth.PasswordResetTokenRepository;
import com.serveflow.repository.user.SpringUserRepository;
import com.serveflow.repository.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LOGO_CID = "serveflow-logo";

    private final PasswordResetTokenRepository tokenRepository;
    private final SpringUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                SpringUserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                JavaMailSender mailSender) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public record IdentifyResult(String username, String maskedEmail) {}

    @Transactional(readOnly = true)
    public IdentifyResult identifyUser(String identifier) {
        String normalized = identifier.toLowerCase().trim();
        return userRepository.findByUsername(normalized)
                .or(() -> userRepository.findByEmail(normalized))
                .map(user -> new IdentifyResult(user.getUsername(), maskEmail(user.getEmail())))
                .orElse(new IdentifyResult(null, null));
    }

    public String requestReset(String identifier) {
        UserEntity user = findUserByIdentifier(identifier);

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
        resetToken.setUsername(user.getUsername());
        resetToken.setToken(code);
        resetToken.setExpiresAt(expiresAt);
        tokenRepository.save(resetToken);

        sendResetEmail(user.getEmail(), user.getUsername(), code);

        log.info("[PasswordReset] reset requested user={} expires={}", user.getUsername(), expiresAt);
        return user.getUsername();
    }

    @Transactional(readOnly = true)
    public void verifyToken(String username, String token) {
        findValidToken(username, token);
    }

    public void resetPassword(String username, String token, String newPassword) {
        PasswordResetTokenEntity entity = findValidToken(username, token);
        UserEntity user = findUserByIdentifier(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        entity.setUsed(true);
        tokenRepository.save(entity);
    }

    private UserEntity findUserByIdentifier(String identifier) {
        String normalized = identifier.toLowerCase().trim();
        return userRepository.findByUsername(normalized)
                .or(() -> userRepository.findByEmail(normalized))
                .orElseThrow(() -> new UserNotFoundException(normalized));
    }

    private PasswordResetTokenEntity findValidToken(String username, String token) {
        return tokenRepository
                .findByUsernameAndTokenAndUsedFalse(username.toLowerCase().trim(), token)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(InvalidResetTokenException::new);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private void sendResetEmail(String toEmail, String username, String code) {
        if (toEmail == null || !toEmail.contains("@")) {
            log.warn("[PasswordReset] No email registered for user '{}'", username);
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("ServeFlow — Código de redefinição de senha");
            helper.setText(buildEmailHtml(username, code), true);
            helper.addInline(LOGO_CID, new ClassPathResource("images/logo.jpeg"));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("[PasswordReset] Failed to send email to '{}'", toEmail, e);
        }
    }

    private String buildEmailHtml(String username, String code) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                </head>
                <body style="margin:0;padding:0;background-color:#f4f4f7;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f7;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0" style="background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 16px rgba(0,0,0,0.10);">

                          <tr>
                            <td style="background-color:#1b5e20;padding:28px 40px;text-align:center;">
                              <img src="cid:%s" alt="ServeFlow" width="90" height="90"
                                   style="border-radius:50%%;border:3px solid #ffffff;display:block;margin:0 auto 12px;"/>
                              <span style="font-size:13px;color:#c8e6c9;letter-spacing:1px;text-transform:uppercase;">Gestão para Pequenos Restaurantes</span>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:40px 40px 24px;">
                              <p style="margin:0 0 12px;font-size:17px;color:#1b2a1b;font-weight:600;">Olá, %s!</p>
                              <p style="margin:0 0 28px;font-size:15px;color:#555555;line-height:1.7;">
                                Recebemos uma solicitação para redefinir a senha da sua conta no <strong>ServeFlow</strong>.
                                Use o código abaixo para continuar o processo:
                              </p>

                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td align="center" style="padding:24px 0;">
                                    <div style="display:inline-block;background-color:#e8f5e9;border:2px solid #2e7d32;border-radius:10px;padding:18px 48px;">
                                      <span style="font-size:36px;font-weight:700;letter-spacing:10px;color:#1b5e20;">%s</span>
                                    </div>
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:8px 0 0;font-size:13px;color:#888888;text-align:center;">
                                Este código expira em <strong>%d minutos</strong>.
                              </p>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:0 40px 32px;">
                              <p style="margin:0;font-size:13px;color:#aaaaaa;line-height:1.6;border-top:1px solid #eeeeee;padding-top:20px;">
                                Se você não solicitou a redefinição de senha, ignore este e-mail com segurança.
                                Sua senha permanece inalterada.
                              </p>
                            </td>
                          </tr>

                          <tr>
                            <td style="background-color:#f9fdf9;padding:18px 40px;text-align:center;border-top:1px solid #e8f5e9;">
                              <p style="margin:0;font-size:12px;color:#aaaaaa;">© 2025 ServeFlow. Todos os direitos reservados.</p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(LOGO_CID, username, code, TOKEN_EXPIRY_MINUTES);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return null;
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) return local.charAt(0) + "***@" + domain;
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }
}
