package com.serveflow.controller.user;

import com.serveflow.service.auth.AuthService;
import com.serveflow.service.auth.AuthService.AuthResult;
import com.serveflow.service.auth.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResult result = authService.authenticate(req.username(), req.password());
        return ResponseEntity.ok(LoginResponse.from(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        AuthResult result = authService.refresh(req.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(result.token(), result.refreshToken()));
    }

    @PostMapping("/identify")
    public ResponseEntity<IdentifyResponse> identify(@Valid @RequestBody IdentifyRequest req) {
        PasswordResetService.IdentifyResult result = passwordResetService.identifyUser(req.identifier());
        return ResponseEntity.ok(new IdentifyResponse(result.username(), result.maskedEmail()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        try {
            String resolvedUsername = passwordResetService.requestReset(req.identifier());
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
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.username(), req.token(), req.newPassword());
        return ResponseEntity.ok().build();
    }

    public record LoginRequest(
            @NotBlank(message = "Usuário é obrigatório") String username,
            @NotBlank(message = "Senha é obrigatória") String password
    ) {}

    public record LoginResponse(String token, String refreshToken, Long id, String username, String role) {
        public static LoginResponse from(AuthResult result) {
            return new LoginResponse(result.token(), result.refreshToken(), result.userId(), result.username(), result.role());
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
            @NotBlank @Size(min = 8, max = 128, message = "A senha deve ter ao menos 8 caracteres") String newPassword
    ) {}
}
