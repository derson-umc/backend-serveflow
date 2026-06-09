package com.serveflow.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.exception.stock.InsufficientStockException;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.order.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    OrderService orderService;
    @Mock
    AuditService auditService;

    @InjectMocks
    OrderController controller;

    MockMvc mvc;
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        User mockUser = new User(1L, "admin", "admin@test.com", "pass", UserRole.ADMIN, "Administrador");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities()));
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler(auditService))
                .build();
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private OrderOutput orderOutput(UUID id, String status) {
        return new OrderOutput(
                id, "Cliente Teste", null, "BALCAO", status,
                LocalDateTime.of(2026, 1, 1, 12, 0), null, null, null, null, null, null,
                new BigDecimal("29.90"), List.of()
        );
    }

    @Test
    @DisplayName("GET /orders: retorna 200 com lista de pedidos")
    void findAll_returns200WithList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(orderService.findAll()).thenReturn(List.of(orderOutput(id1, "CREATED"), orderOutput(id2, "CONFIRMED")));

        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(orderService).findAll();
    }

    @Test
    @DisplayName("GET /orders: retorna 200 com lista vazia quando não há pedidos")
    void findAll_returns200WithEmptyList() throws Exception {
        when(orderService.findAll()).thenReturn(List.of());

        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /orders/{id}: retorna 200 com pedido quando encontrado")
    void findById_returns200WithOrder() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.findById(id)).thenReturn(orderOutput(id, "CREATED"));

        mvc.perform(get("/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.customerName").value("Cliente Teste"));

        verify(orderService).findById(id);
    }

    @Test
    @DisplayName("GET /orders/{id}: retorna 404 quando pedido não existe")
    void findById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.findById(id)).thenThrow(new OrderNotFoundException(id));

        mvc.perform(get("/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /orders/status/{status}: retorna 200 com pedidos filtrados")
    void findByStatus_returns200WithFilteredList() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.findByStatus("CREATED")).thenReturn(List.of(orderOutput(id, "CREATED")));

        mvc.perform(get("/orders/status/{status}", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"));

        verify(orderService).findByStatus("CREATED");
    }

    @Test
    @DisplayName("PATCH /orders/{id}/confirm: retorna 200 com pedido confirmado")
    void confirm_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.confirm(id)).thenReturn(orderOutput(id, "CONFIRMED"));

        mvc.perform(patch("/orders/{id}/confirm", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).confirm(id);
    }

    @Test
    @DisplayName("PATCH /orders/{id}/confirm: retorna 404 quando pedido não existe")
    void confirm_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.confirm(id)).thenThrow(new OrderNotFoundException(id));

        mvc.perform(patch("/orders/{id}/confirm", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /orders/{id}/confirm: retorna 422 quando estoque insuficiente")
    void confirm_returns422WhenInsufficientStock() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.confirm(id)).thenThrow(new InsufficientStockException("Estoque insuficiente para: Farinha"));

        mvc.perform(patch("/orders/{id}/confirm", id))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("PATCH /orders/{id}/prepare: retorna 200 com pedido em preparo")
    void prepare_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.startPreparation(id)).thenReturn(orderOutput(id, "PREPARING"));

        mvc.perform(patch("/orders/{id}/prepare", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));

        verify(orderService).startPreparation(id);
    }

    @Test
    @DisplayName("PATCH /orders/{id}/ready: retorna 200 com pedido pronto")
    void ready_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.markReady(id)).thenReturn(orderOutput(id, "READY"));

        mvc.perform(patch("/orders/{id}/ready", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        verify(orderService).markReady(id);
    }

    @Test
    @DisplayName("PATCH /orders/{id}/complete: retorna 200 com pedido finalizado")
    void complete_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.complete(id)).thenReturn(orderOutput(id, "COMPLETED"));

        mvc.perform(patch("/orders/{id}/complete", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(orderService).complete(id);
    }

    @Test
    @DisplayName("PATCH /orders/{id}/cancel: retorna 200 com pedido cancelado")
    void cancel_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.cancel(eq(id), any(), any())).thenReturn(orderOutput(id, "CANCELADO"));

        mvc.perform(patch("/orders/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        verify(orderService).cancel(eq(id), any(), any());
    }

    @Test
    @DisplayName("PATCH /orders/{id}/cancel: retorna 404 quando pedido não existe")
    void cancel_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.cancel(eq(id), any(), any())).thenThrow(new OrderNotFoundException(id));

        mvc.perform(patch("/orders/{id}/cancel", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
