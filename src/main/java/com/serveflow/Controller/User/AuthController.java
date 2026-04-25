package com.serveflow.Controller.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.serveflow.Config.Jwt.JwtService;
import com.serveflow.Dto.PasswordReset.Request.ChangePasswordRequest;
import com.serveflow.Dto.PasswordReset.Request.ForgotPasswordRequest;
import com.serveflow.Dto.PasswordReset.Request.ResetPasswordRequest;
import com.serveflow.Dto.User.Request.UserInput;
import com.serveflow.Dto.User.Response.UserOutput;
import com.serveflow.Exception.User.BusinessRuleException;
import com.serveflow.Exception.User.UserNotFoundException;
import com.serveflow.Model.User.User;
import com.serveflow.Repository.User.UserRepository;
import com.serveflow.Service.PasswordReset.PasswordResetService;
import com.serveflow.Service.User.UserService;

import java.util.Map;

@Tag(name = "Autenticação", description = "Login, registro e gestão de senhas")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "Login", description = "Autentica e devolve um JWT")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest req) {
        User user = repo.findByUsername(req.username())
                .orElseThrow(() -> new UserNotFoundException(req.username()));

        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new BusinessRuleException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole().name(),
                "username", user.getUsername()
        ));
    }

    @Operation(summary = "Registro de usuário", description = "Cria novo usuário (apenas ADMIN/ROOT)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/register")
    public ResponseEntity<UserOutput> register(@Valid @RequestBody UserInput req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(req));
    }

    @Operation(summary = "Esqueci minha senha", description = "Gera link de reset (resposta sempre 202 para não vazar usuários)")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.requestReset(req.username());
        return ResponseEntity.accepted().body(Map.of(
                "message", "Se o usuário existir, um link de redefinição foi enviado."
        ));
    }

    @Operation(summary = "Redefinir senha", description = "Redefine a senha usando token recebido por e-mail")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }

    @Operation(summary = "Alterar senha", description = "Usuário autenticado altera sua própria senha")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest req) {
        passwordResetService.changePassword(user.getUsername(), req.currentPassword(), req.newPassword());
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso."));
    }

    public record LoginRequest(
            @jakarta.validation.constraints.NotBlank String username,
            @jakarta.validation.constraints.NotBlank String password
    ) {}
}
