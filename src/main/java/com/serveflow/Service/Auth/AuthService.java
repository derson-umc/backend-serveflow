package com.serveflow.Service.Auth;

import com.serveflow.Config.JwtService;
import com.serveflow.Exception.User.BusinessRuleException;
import com.serveflow.Exception.User.UserNotFoundException;
import com.serveflow.Model.User.User;
import com.serveflow.Repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResult authenticate(String rawUsername, String rawPassword) {
        String username = normalizeUsername(rawUsername);
        String password = normalizePassword(rawPassword);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessRuleException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResult(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    private static String normalizeUsername(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePassword(String value) {
        return value == null ? "" : value.trim();
    }

    public record AuthResult(String token, Long userId, String username, String role) {}
}
