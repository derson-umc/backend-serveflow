package com.serveflow.Service.User;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.serveflow.Dto.User.Request.ChangeJobPositionInput;
import com.serveflow.Dto.User.Request.ChangePasswordInput;
import com.serveflow.Dto.User.Request.UserInput;
import com.serveflow.Dto.User.Response.UserOutput;
import com.serveflow.Exception.User.BusinessRuleException;
import com.serveflow.Exception.User.ConflictException;
import com.serveflow.Exception.User.UserNotFoundException;
import com.serveflow.Model.User.User;
import com.serveflow.Model.User.UserRole;
import com.serveflow.Repository.User.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public UserOutput create(UserInput request) {
        if (repo.existsByUsername(request.username())) {
            throw new ConflictException("Username '" + request.username() + "' já está em uso");
        }
        User user = User.create(
                request.username(),
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
        return repo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    public List<UserOutput> findAll() {
        return repo.findAll().stream()
                .map(UserOutput::from)
                .toList();
    }

    @Transactional
    public UserOutput update(Long id, UserInput request) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!existing.getUsername().equals(request.username())
                && repo.existsByUsername(request.username())) {
            throw new ConflictException("Username '" + request.username() + "' já está em uso");
        }

        String password = (request.password() != null && !request.password().isBlank())
                ? encoder.encode(request.password())
                : existing.getPassword();

        User updated = new User(
                existing.getId(),
                request.username(),
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
            throw new BusinessRuleException("Senha atual incorreta");
        }
        if (encoder.matches(request.newPassword(), existing.getPassword())) {
            throw new BusinessRuleException("A nova senha deve ser diferente da atual");
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

        if (user.getRole() == UserRole.ROOT) {
            throw new BusinessRuleException("Usuário com perfil ROOT não pode ser excluído");
        }

        repo.deleteById(id);
    }
}
