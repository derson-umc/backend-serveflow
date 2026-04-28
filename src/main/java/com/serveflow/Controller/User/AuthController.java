package com.serveflow.Controller.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.serveflow.Config.Jwt.JwtService;
import com.serveflow.Exception.User.BusinessRuleException;
import com.serveflow.Exception.User.UserNotFoundException;
import com.serveflow.Model.User.User;
import com.serveflow.Repository.User.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

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

    public record LoginRequest(String username, String password) {}
}
