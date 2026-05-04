//package com.serveflow.Service.User;
//
//import com.serveflow.Dto.User.Request.ChangeJobPositionInput;
//import com.serveflow.Dto.User.Request.ChangePasswordInput;
//import com.serveflow.Dto.User.Request.UserInput;
//import com.serveflow.Dto.User.Response.UserOutput;
//import com.serveflow.Exception.User.BusinessRuleException;
//import com.serveflow.Exception.User.ConflictException;
//import com.serveflow.Exception.User.UserNotFoundException;
//import com.serveflow.Model.User.User;
//import com.serveflow.Model.User.UserRole;
//import com.serveflow.Repository.User.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceTest {
//
//    @Mock UserRepository repo;
//    @Mock PasswordEncoder encoder;
//
//    @InjectMocks UserService service;
//
//    private User existing;
//
//    @BeforeEach
//    void setup() {
//        existing = new User(1L, "joao", "encoded-old", UserRole.GARCON, "Garçom");
//    }
//
//    @Test
//    void create_quandoUsernameJaExiste_lancaConflict() {
//        when(repo.existsByUsername("joao")).thenReturn(true);
//
//        assertThatThrownBy(() -> service.create(new UserInput("joao", "Senha@12345", UserRole.GARCON, "Garçom")))
//                .isInstanceOf(ConflictException.class)
//                .hasMessageContaining("já está em uso");
//
//        verify(repo, never()).save(any());
//    }
//
//    @Test
//    void create_persisteSenhaCriptografada() {
//        when(repo.existsByUsername("maria")).thenReturn(false);
//        when(encoder.encode("Senha@12345")).thenReturn("hash");
//        when(repo.save(any(User.class))).thenAnswer(inv -> {
//            User u = inv.getArgument(0);
//            return new User(99L, u.getUsername(), u.getPassword(), u.getRole(), u.getJobposition());
//        });
//
//        UserOutput out = service.create(new UserInput("maria", "Senha@12345", UserRole.CAIXA, "Caixa A"));
//
//        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
//        verify(repo).save(captor.capture());
//        assertThat(captor.getValue().getPassword()).isEqualTo("hash");
//        assertThat(out.username()).isEqualTo("maria");
//        assertThat(out.jobposition()).isEqualTo("Caixa A");
//    }
//
//    @Test
//    void changePassword_senhaAtualIncorreta_lancaBusinessRule() {
//        when(repo.findById(1L)).thenReturn(Optional.of(existing));
//        when(encoder.matches("errada", "encoded-old")).thenReturn(false);
//
//        assertThatThrownBy(() -> service.changePassword(1L, new ChangePasswordInput("errada", "Nova@12345")))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessage("Senha atual incorreta");
//
//        verify(repo, never()).save(any());
//    }
//
//    @Test
//    void changePassword_novaIgualAtual_lancaBusinessRule() {
//        when(repo.findById(1L)).thenReturn(Optional.of(existing));
//        when(encoder.matches("Atual@12345", "encoded-old")).thenReturn(true);
//        when(encoder.matches("Atual@12345", "encoded-old")).thenReturn(true);
//
//        assertThatThrownBy(() -> service.changePassword(1L,
//                new ChangePasswordInput("Atual@12345", "Atual@12345")))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessageContaining("diferente");
//    }
//
//    @Test
//    void changePassword_persisteNovaCriptografadaSemAlterarRoleOuCargo() {
//        when(repo.findById(1L)).thenReturn(Optional.of(existing));
//        when(encoder.matches("Atual@12345", "encoded-old")).thenReturn(true);
//        when(encoder.matches("Nova@2025X", "encoded-old")).thenReturn(false);
//        when(encoder.encode("Nova@2025X")).thenReturn("encoded-new");
//        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
//
//        service.changePassword(1L, new ChangePasswordInput("Atual@12345", "Nova@2025X"));
//
//        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
//        verify(repo).save(captor.capture());
//        User saved = captor.getValue();
//        assertThat(saved.getPassword()).isEqualTo("encoded-new");
//        assertThat(saved.getRole()).isEqualTo(UserRole.GARCON);
//        assertThat(saved.getJobposition()).isEqualTo("Garçom");
//    }
//
//    @Test
//    void changeJobPosition_alteraSomenteCargo() {
//        when(repo.findById(1L)).thenReturn(Optional.of(existing));
//        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
//
//        UserOutput out = service.changeJobPosition(1L, new ChangeJobPositionInput("Maître"));
//
//        assertThat(out.jobposition()).isEqualTo("Maître");
//        assertThat(out.role()).isEqualTo(UserRole.GARCON);
//        verify(encoder, never()).encode(any());
//    }
//
//    @Test
//    void delete_usuarioRoot_proibido() {
//        User root = new User(1L, "root", "x", UserRole.ROOT, "ROOT");
//        when(repo.findById(1L)).thenReturn(Optional.of(root));
//
//        assertThatThrownBy(() -> service.delete(1L))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessageContaining("ROOT");
//
//        verify(repo, never()).deleteById(any());
//    }
//
//    @Test
//    void findById_inexistente_lancaNotFound() {
//        when(repo.findById(999L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> service.findById(999L))
//                .isInstanceOf(UserNotFoundException.class);
//    }
//}
