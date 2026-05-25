package com.serveflow.controller.user;

import com.serveflow.util.IpResolverUtil;
import com.serveflow.model.user.User;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.auth.AuthService;
import com.serveflow.service.auth.AuthService.AuthResult;
import com.serveflow.service.auth.PasswordResetService;
import com.serveflow.service.auth.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService         authService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService  refreshTokenService;
    private final AuditService         auditService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletRequest httpReq) {
        String ip = IpResolverUtil.getClientIp(httpReq);
        try {
            AuthResult result = authService.authenticate(req.username(), req.password());
            auditService.logLoginSuccess(result.userId(), result.username(), ip);
            auditService.logAccess(result.userId(), ip, "/auth/login", "POST", 200);
            return ResponseEntity.ok(LoginResponse.from(result));
        } catch (Exception ex) {
            auditService.logLoginFailure(req.username(), ip, ex.getMessage());
            auditService.logAccess(null, ip, "/auth/login", "POST", 401);
            throw ex;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        AuthResult result = authService.refresh(req.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(result.token(), result.refreshToken()));
    }

    @PostMapping("/identify")
    public ResponseEntity<IdentifyResponse> identify(@Valid @RequestBody IdentifyRequest req) {
        PasswordResetService.IdentifyResult result =
                passwordResetService.identifyUser(req.identifier());
        return ResponseEntity.ok(new IdentifyResponse(result.username(), result.maskedEmail()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req,
                                            HttpServletRequest httpReq) {
        String ip = IpResolverUtil.getClientIp(httpReq);
        try {
            String resolvedUsername = passwordResetService.requestReset(req.identifier());
            auditService.logPasswordReset(resolvedUsername, ip, false);
            return ResponseEntity.accepted().body(new ForgotPasswordResponse(resolvedUsername));
        } catch (Exception ignored) {
            return ResponseEntity.accepted().build();
        }
    }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<Void> verifyResetToken(@Valid @RequestBody VerifyTokenRequest req) {
        passwordResetService.verifyToken(req.username(), req.token());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req,
                                              HttpServletRequest httpReq) {
        String ip = IpResolverUtil.getClientIp(httpReq);
        passwordResetService.resetPassword(req.username(), req.token(), req.newPassword());
        auditService.logPasswordReset(req.username(), ip, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user,
                                       HttpServletRequest httpReq) {
        if (user != null) {
            refreshTokenService.revokeAll(user.getId());
            auditService.logLogout(user.getId(), user.getUsername(),
                    IpResolverUtil.getClientIp(httpReq));
        }
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(
            @NotBlank(message = "Usuário é obrigatório") String username,
            @NotBlank(message = "Senha é obrigatória")   String password
    ) {}

    public record LoginResponse(String token, String refreshToken,
                                Long id, String username, String role) {
        public static LoginResponse from(AuthResult result) {
            return new LoginResponse(result.token(), result.refreshToken(),
                    result.userId(), result.username(), result.role());
        }
    }

    public record RefreshRequest(
            @NotBlank(message = "Refresh token é obrigatório") String refreshToken
    ) {}

    public record RefreshResponse(String token, String refreshToken) {}

    public record IdentifyRequest(
            @NotBlank(message = "Identificação é obrigatória") String identifier
    ) {}

    public record IdentifyResponse(String username, String maskedEmail) {}

    public record ForgotPasswordRequest(
            @NotBlank(message = "Identificação é obrigatória") String identifier
    ) {}

    public record ForgotPasswordResponse(String username) {}

    public record VerifyTokenRequest(
            @NotBlank String username,
            @NotBlank @Size(min = 6, max = 6, message = "O código deve ter 6 dígitos") String token
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String username,
            @NotBlank @Size(min = 6, max = 6) String token,
            @NotBlank @Size(min = 8, max = 128, message = "A senha deve ter ao menos 8 caracteres")
            String newPassword
    ) {}
}
