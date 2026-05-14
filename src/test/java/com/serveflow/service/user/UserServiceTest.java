package com.serveflow.service.user;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repo;
    @Mock PasswordEncoder encoder;

    @InjectMocks UserService service;

    @BeforeEach
    void setupAuthContext() {
        User admin = new User(99L, "rootadmin", "x", UserRole.ADMIN, "Admin");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities())
        );
    }

    @Test
    @DisplayName("create normaliza username e salva")
    void create_ok() {
        when(repo.existsByUsername("joao")).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("hash");
        when(repo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(1L, u.getUsername(), u.getPassword(), u.getRole(), u.getJobposition());
        });

        UserOutput out = service.create(new UserInput("  JOAO  ", "123", UserRole.GARCON, "Garçom"));

        assertThat(out.username()).isEqualTo("joao");
        verify(repo).existsByUsername("joao");
    }

    @Test
    @DisplayName("create conflito username")
    void create_conflict() {
        when(repo.existsByUsername("joao")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new UserInput("JOAO", "123", UserRole.GARCON, "Garçom"))
        ).isInstanceOf(ConflictException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("create gerente não pode criar admin")
    void create_gerenteNaoPodeAdmin() {
        User gerente = new User(2L, "ger", "x", UserRole.GERENTE, "Gerente");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(gerente, null, gerente.getAuthorities())
        );

        assertThatThrownBy(() -> service.create(
                new UserInput("novo", "123", UserRole.ADMIN, "Admin"))
        ).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("findById ok")
    void findById_ok() {
        User user = new User(1L, "joao", "x", UserRole.GARCON, "Garçom");
        when(repo.findById(1L)).thenReturn(Optional.of(user));

        UserOutput out = service.findById(1L);

        assertThat(out.username()).isEqualTo("joao");
    }

    @Test
    @DisplayName("findById not found")
    void findById_notFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("findByUsername ok")
    void findByUsername_ok() {
        User user = new User(1L, "joao", "x", UserRole.GARCON, "Garçom");
        when(repo.findByUsername("joao")).thenReturn(Optional.of(user));

        User result = service.findByUsername(" JOAO ");

        assertThat(result.getUsername()).isEqualTo("joao");
    }

    @Test
    @DisplayName("findByUsername not found")
    void findByUsername_notFound() {
        when(repo.findByUsername("joao")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUsername("joao"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("findAll retorna lista")
    void findAll_ok() {
        when(repo.findAll()).thenReturn(List.of(
                new User(1L, "a", "x", UserRole.GARCON, "Garçom"),
                new User(2L, "b", "x", UserRole.COZINHEIRO, "Cozinha")
        ));

        List<UserOutput> result = service.findAll();

        assertThat(result).hasSize(2);
    }


    @Test
    @DisplayName("update conflito username")
    void update_conflict() {
        User existing = new User(1L, "joao", "old", UserRole.GARCON, "Garçom");
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.existsByUsername("maria")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L,
                new UserInput("maria", "x", UserRole.GARCON, "Garçom")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("changePassword ok")
    void changePassword_ok() {
        User user = new User(1L, "joao", "old", UserRole.GARCON, "Garçom");
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(encoder.matches("old", "old")).thenReturn(true);
        when(encoder.matches("new", "old")).thenReturn(false);
        when(encoder.encode("new")).thenReturn("hash");

        service.changePassword(1L, new ChangePasswordInput("old", "new"));

        verify(repo).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword senha atual incorreta")
    void changePassword_wrongCurrent() {
        User user = new User(1L, "joao", "old", UserRole.GARCON, "Garçom");
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(encoder.matches("err", "old")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(1L,
                new ChangePasswordInput("err", "new")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("changePassword nova igual atual")
    void changePassword_samePassword() {
        User user = new User(1L, "joao", "old", UserRole.GARCON, "Garçom");
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(encoder.matches("old", "old")).thenReturn(true);
        when(encoder.matches("old", "old")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(1L,
                new ChangePasswordInput("old", "old")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("resetPassword ok")
    void resetPassword_ok() {
        User user = new User(1L, "joao", "old", UserRole.GARCON, "Garçom");
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(encoder.encode("new")).thenReturn("hash");

        service.resetPassword(1L, "new");

        verify(repo).save(any(User.class));
    }

    @Test
    @DisplayName("resetPassword gerente não pode admin")
    void resetPassword_gerenteNaoAdmin() {
        User gerente = new User(2L, "ger", "x", UserRole.GERENTE, "Gerente");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(gerente, null, gerente.getAuthorities())
        );

        User admin = new User(1L, "admin", "x", UserRole.ADMIN, "Admin");
        when(repo.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.resetPassword(1L, "new"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("changeJobPosition ok")
    void changeJobPosition_ok() {
        User user = new User(1L, "joao", "x", UserRole.GARCON, "Garçom");
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserOutput out = service.changeJobPosition(1L,
                new ChangeJobPositionInput("Novo"));

        assertThat(out.jobposition()).isEqualTo("Novo");
    }

    @Test
    @DisplayName("delete ok")
    void delete_ok() {
        User user = new User(1L, "joao", "x", UserRole.GARCON, "Garçom");
        when(repo.findById(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    @DisplayName("delete bloqueia admin e gerente")
    void delete_blocked() {
        User admin = new User(1L, "admin", "x", UserRole.ADMIN, "Admin");
        when(repo.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessRuleException.class);
    }
}

