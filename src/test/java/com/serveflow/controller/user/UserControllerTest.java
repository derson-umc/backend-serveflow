package com.serveflow.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serveflow.dto.user.request.ChangeJobPositionInput;
import com.serveflow.dto.user.request.ChangePasswordInput;
import com.serveflow.dto.user.request.ResetPasswordInput;
import com.serveflow.dto.user.request.UserInput;
import com.serveflow.dto.user.response.UserOutput;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.exception.user.UserNotFoundException;
import com.serveflow.service.audit.AuditService;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService service;
    @Mock
    AuditService auditService;

    @InjectMocks
    UserController controller;

    MockMvc mvc;
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(auditService))
                .build();
        mapper = new ObjectMapper();
    }

    private UserInput validInput() {
        return new UserInput("joaosilva", null, "senha1234", UserRole.CAIXA, "Operador de Caixa");
    }

    private UserOutput output(Long id) {
        return new UserOutput(id, "joaosilva", null, UserRole.CAIXA, "Operador de Caixa");
    }

    private String json(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    @Test
    @DisplayName("POST /users: retorna 201 com body ao criar usuário válido")
    void create_returns201WithBody() throws Exception {
        when(service.create(any(UserInput.class))).thenReturn(output(1L));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validInput())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("joaosilva"));

        verify(service).create(any(UserInput.class));
    }

    @Test
    @DisplayName("POST /users: retorna 400 quando campos obrigatórios estão ausentes")
    void create_returns400WhenRequiredFieldsMissing() throws Exception {
        String body = "{\"username\":\"\",\"password\":\"\",\"role\":null,\"jobposition\":\"\"}";

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists());
    }

    @Test
    @DisplayName("POST /users: retorna 400 com body malformado")
    void create_returns400WhenBodyMalformed() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ malformed }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /users: retorna 200 com lista de usuários")
    void findAll_returns200WithList() throws Exception {
        when(service.findAll()).thenReturn(List.of(output(1L), output(2L)));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).findAll();
    }

    @Test
    @DisplayName("GET /users: retorna 200 com lista vazia quando não há usuários")
    void findAll_returns200WithEmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /users/{id}: retorna 200 com usuário quando encontrado")
    void findById_returns200WithUser() throws Exception {
        when(service.findById(1L)).thenReturn(output(1L));

        mvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("joaosilva"));

        verify(service).findById(1L);
    }

    @Test
    @DisplayName("GET /users/{id}: retorna 404 quando usuário não existe")
    void findById_returns404WhenNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new UserNotFoundException(99L));

        mvc.perform(get("/users/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /users/{id}: retorna 200 com usuário atualizado")
    void update_returns200WithUpdatedUser() throws Exception {
        when(service.update(eq(1L), any(UserInput.class))).thenReturn(output(1L));

        mvc.perform(put("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validInput())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).update(eq(1L), any(UserInput.class));
    }

    @Test
    @DisplayName("PUT /users/{id}: retorna 404 quando usuário não existe")
    void update_returns404WhenNotFound() throws Exception {
        when(service.update(eq(99L), any(UserInput.class))).thenThrow(new UserNotFoundException(99L));

        mvc.perform(put("/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validInput())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /users/{id}/password: retorna 204 ao alterar senha com sucesso")
    void changePassword_returns204() throws Exception {
        doNothing().when(service).changePassword(eq(1L), any(ChangePasswordInput.class));

        mvc.perform(patch("/users/{id}/password", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChangePasswordInput("velhasenha", "novasenha1"))))
                .andExpect(status().isNoContent());

        verify(service).changePassword(eq(1L), any(ChangePasswordInput.class));
    }

    @Test
    @DisplayName("PATCH /users/{id}/reset-password: retorna 204 ao resetar senha")
    void resetPassword_returns204() throws Exception {
        doNothing().when(service).resetPassword(eq(1L), any(String.class));

        mvc.perform(patch("/users/{id}/reset-password", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ResetPasswordInput("novasenha1"))))
                .andExpect(status().isNoContent());

        verify(service).resetPassword(eq(1L), any(String.class));
    }

    @Test
    @DisplayName("PATCH /users/{id}/job-position: retorna 200 com usuário atualizado")
    void changeJobPosition_returns200() throws Exception {
        when(service.changeJobPosition(eq(1L), any(ChangeJobPositionInput.class))).thenReturn(output(1L));

        mvc.perform(patch("/users/{id}/job-position", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChangeJobPositionInput("Gerente de Loja"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).changeJobPosition(eq(1L), any(ChangeJobPositionInput.class));
    }

}
