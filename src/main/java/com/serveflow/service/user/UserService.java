package com.serveflow.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.serveflow.dto.user.request.ChangeJobPositionInput;
import com.serveflow.dto.user.request.ChangePasswordInput;
import com.serveflow.dto.user.request.UserInput;
import com.serveflow.dto.user.response.UserOutput;
import com.serveflow.exception.user.BusinessRuleException;
import com.serveflow.exception.user.ConflictException;
import com.serveflow.exception.user.ForbiddenOperationException;
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.repository.user.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;

import com.serveflow.util.UsernameUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    private static final Set<UserRole> ADMIN_ONLY_ROLES = Set.of(UserRole.ADMIN);

    private static String normalizeEmail(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private void validateRolePermission(UserRole targetRole) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User currentUser) {
            if (currentUser.getRole() == UserRole.GERENTE && ADMIN_ONLY_ROLES.contains(targetRole)) {
                throw new BusinessRuleException("Gerente não pode atribuir o perfil ADMIN");
            }
        }
    }

    private void validateNotAdminTarget(User target) {
        if (target.getRole() != UserRole.ADMIN) return;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User currentUser && currentUser.getRole() == UserRole.GERENTE) {
            throw new BusinessRuleException("Gerente não pode editar usuários com perfil ADMIN");
        }
    }

    @Transactional
    public UserOutput create(UserInput request) {
        validateRolePermission(request.role());
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessRuleException("Senha é obrigatória");
        }
        String username = UsernameUtils.normalize(request.username());
        if (repository.existsByUsername(username)) {
            throw new ConflictException("Username '%s' já está em uso".formatted(username));
        }
        User user = User.create(
                username,
                normalizeEmail(request.email()),
                encoder.encode(request.password()),
                request.role(),
                request.jobposition()
        );
        return UserOutput.from(repository.save(user));
    }

    public UserOutput findById(Long id) {
        return UserOutput.from(
                repository.findById(id).orElseThrow(() -> new UserNotFoundException(id))
        );
    }

    public User findByUsername(String username) {
        String normalized = UsernameUtils.normalize(username);
        return repository.findByUsername(normalized)
                .orElseThrow(() -> new UserNotFoundException(normalized));
    }

    public List<UserOutput> findAll() {
        return repository.findAll().stream()
                .map(UserOutput::from)
                .toList();
    }

    @Transactional
    public UserOutput update(Long id, UserInput request) {
        validateRolePermission(request.role());
        User existing = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        validateNotAdminTarget(existing);

        String username = UsernameUtils.normalize(request.username());
        if (!existing.getUsername().equals(username) && repository.existsByUsername(username)) {
            throw new ConflictException("Username '%s' já está em uso".formatted(username));
        }

        String password = (request.password() != null && !request.password().isBlank())
                ? encoder.encode(request.password())
                : existing.getPassword();

        return UserOutput.from(repository.save(
                existing.update(username, normalizeEmail(request.email()), password, request.role(), request.jobposition())
        ));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordInput request) {
        User existing = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!encoder.matches(request.currentPassword(), existing.getPassword())) {
            throw new BusinessRuleException("Senha atual incorreta.");
        }
        if (encoder.matches(request.newPassword(), existing.getPassword())) {
            throw new BusinessRuleException("A nova senha deve ser diferente da atual.");
        }

        repository.save(existing.withPassword(encoder.encode(request.newPassword())));
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User existing = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        validateNotAdminTarget(existing);

        repository.save(existing.withPassword(encoder.encode(newPassword)));
    }

    @Transactional
    public UserOutput changeJobPosition(Long id, ChangeJobPositionInput request) {
        User existing = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return UserOutput.from(repository.save(existing.withJobPosition(request.jobposition())));
    }

    @Transactional
    public void delete(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.GERENTE) {
            throw new ForbiddenOperationException("Exclusão de usuário com perfil privilegiado não é permitida.");
        }

        repository.deleteById(id);
    }
}
