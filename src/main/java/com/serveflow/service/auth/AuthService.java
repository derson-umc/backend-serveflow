package com.serveflow.service.auth;

import com.serveflow.config.JwtService;
import com.serveflow.exception.user.BusinessRuleException;
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.model.user.User;
import com.serveflow.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serveflow.util.UsernameUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthResult authenticate(String rawUsername, String rawPassword) {
        String username = UsernameUtils.normalize(rawUsername);
        if (username == null) throw new BusinessRuleException("Credenciais inválidas");
        String password = normalizePassword(rawPassword);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessRuleException("Credenciais inválidas");
        }

        String accessToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = tryCreateRefreshToken(user.getId());
        return new AuthResult(accessToken, refreshToken, user.getId(), user.getUsername(), user.getRole().name());
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        Long userId = refreshTokenService.validateAndRotate(rawRefreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String accessToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = tryCreateRefreshToken(user.getId());
        return new AuthResult(accessToken, newRefreshToken, user.getId(), user.getUsername(), user.getRole().name());
    }

    private String tryCreateRefreshToken(Long userId) {
        try {
            return refreshTokenService.create(userId);
        } catch (Exception e) {
            log.warn("Refresh token não pôde ser criado para userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private static String normalizePassword(String value) {
        return value == null ? "" : value.trim();
    }

    public record AuthResult(String token, String refreshToken, Long userId, String username, String role) {}
}
