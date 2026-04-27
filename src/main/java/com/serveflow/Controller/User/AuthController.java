package com.serveflow.Controller.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.serveflow.Config.Jwt.JwtService;
import com.serveflow.Dto.User.Request.ForgotPasswordInput;
import com.serveflow.Dto.User.Request.ResetPasswordInput;
import com.serveflow.Exception.User.BusinessRuleException;
import com.serveflow.Exception.User.UserNotFoundException;
import com.serveflow.Model.User.User;
import com.serveflow.Repository.User.UserRepository;
import com.serveflow.Service.User.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {
        User user = repo.findByUsername(req.username())
                .orElseThrow(() -> new UserNotFoundException(req.username()));

        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new BusinessRuleException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole().name(),
                "username", user.getUsername(),
                "id", String.valueOf(user.getId())
        ));
    }

    /**
     * Sempre retorna 204 — não revela se o usuário existe.
     * O e-mail (quando configurado) leva o token de reset válido por 15 minutos.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordInput req) {
        passwordResetService.requestReset(req.username());
        return ResponseEntity.noContent().build();
    }

    /**
     * Aplica a nova senha se o token for válido. Em qualquer falha
     * (token inválido, expirado, já usado), retorna 422 com mensagem genérica.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordInput req) {
        passwordResetService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(String username, String password) {}
}
