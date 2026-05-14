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
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.repository.user.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    private static final Set<UserRole> ADMIN_ONLY_ROLES = Set.of(UserRole.ADMIN);

    private static String normalizeUsername(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private void validateRolePermission(UserRole targetRole) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User currentUser) {
            if (currentUser.getRole() == UserRole.GERENTE && ADMIN_ONLY_ROLES.contains(targetRole)) {
                throw new BusinessRuleException("Gerente não pode atribuir o perfil ADMIN");
            }
        }
    }

    @Transactional
    public UserOutput create(UserInput request) {
        validateRolePermission(request.role());
        String username = normalizeUsername(request.username());
        if (repo.existsByUsername(username)) {
            throw new ConflictException("Username '" + username + "' já está em uso");
        }
        User user = User.create(
                username,
                encoder.encode(request.password()),
                request.role(),
                request.jobposition()
        );
        return UserOutput.from(repo.save(user));
    }

    public UserOutput findById(Long id) {
        return UserOutput.from(
                repo.findById(id).orElseThrow(() -> new UserNotFoundException(id))
        );
    }

    public User findByUsername(String username) {
        String normalized = normalizeUsername(username);
        return repo.findByUsername(normalized)
                .orElseThrow(() -> new UserNotFoundException(normalized));
    }

    public List<UserOutput> findAll() {
        return repo.findAll().stream()
                .map(UserOutput::from)
                .toList();
    }

    @Transactional
    public UserOutput update(Long id, UserInput request) {
        validateRolePermission(request.role());
        User existing = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (existing.getRole() == UserRole.ADMIN) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User currentUser && currentUser.getRole() == UserRole.GERENTE) {
                throw new BusinessRuleException("Gerente não pode editar usuários com perfil ADMIN");
            }
        }

        String username = normalizeUsername(request.username());
        if (!existing.getUsername().equals(username)
                && repo.existsByUsername(username)) {
            throw new ConflictException("Username '" + username + "' já está em uso");
        }

        String password = (request.password() != null && !request.password().isBlank())
                ? encoder.encode(request.password())
                : existing.getPassword();

        User updated = new User(
                existing.getId(),
                username,
                password,
                request.role(),
                request.jobposition()
        );
        return UserOutput.from(repo.save(updated));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordInput request) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!encoder.matches(request.currentPassword(), existing.getPassword())) {
            throw new BusinessRuleException("Senha atual incorreta.");
        }
        if (encoder.matches(request.newPassword(), existing.getPassword())) {
            throw new BusinessRuleException("A nova senha deve ser diferente da atual.");
        }

        User updated = new User(
                existing.getId(),
                existing.getUsername(),
                encoder.encode(request.newPassword()),
                existing.getRole(),
                existing.getJobposition()
        );
        repo.save(updated);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (existing.getRole() == UserRole.ADMIN) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User currentUser && currentUser.getRole() == UserRole.GERENTE) {
                throw new BusinessRuleException("Gerente não pode redefinir senha de usuário ADMIN");
            }
        }

        User updated = new User(
                existing.getId(),
                existing.getUsername(),
                encoder.encode(newPassword),
                existing.getRole(),
                existing.getJobposition()
        );
        repo.save(updated);
    }

    @Transactional
    public UserOutput changeJobPosition(Long id, ChangeJobPositionInput request) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        User updated = new User(
                existing.getId(),
                existing.getUsername(),
                existing.getPassword(),
                existing.getRole(),
                request.jobposition()
        );
        return UserOutput.from(repo.save(updated));
    }

    @Transactional
    public void delete(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.GERENTE) {
            throw new BusinessRuleException("Usuário com perfil " + user.getRole().name() + " não pode ser excluído.");
        }

        repo.deleteById(id);
    }
}
