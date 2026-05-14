package com.serveflow.controller.user;

import com.serveflow.service.auth.AuthService;
import com.serveflow.service.auth.AuthService.AuthResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResult result = authService.authenticate(req.username(), req.password());
        return ResponseEntity.ok(LoginResponse.from(result));
    }

    public record LoginRequest(
            @NotBlank(message = "Usuário é obrigatório") String username,
            @NotBlank(message = "Senha é obrigatória") String password
    ) {}

    public record LoginResponse(String token, Long id, String username, String role) {
        public static LoginResponse from(AuthResult result) {
            return new LoginResponse(result.token(), result.userId(), result.username(), result.role());
        }
    }
}
