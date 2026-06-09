package com.serveflow.controller.cashier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.cashier.request.CashMovementInput;
import com.serveflow.dto.cashier.request.CloseSessionInput;
import com.serveflow.dto.cashier.request.OpenSessionInput;
import com.serveflow.dto.cashier.request.SettleOrderInput;
import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.dto.cashier.response.CashSessionOutput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.exception.cashier.CashSessionNotFoundException;
import com.serveflow.exception.cashier.OpenSessionAlreadyExistsException;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.model.financial.TransactionType;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.cashier.CashierService;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CashierControllerTest {

    @Mock
    CashierService cashierService;
    @Mock
    AuditService auditService;
    @Mock
    CashierEventPublisher eventPublisher;
    @Mock
    OrderService orderService;

    @InjectMocks
    CashierController controller;

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

    private CashSessionOutput sessionOutput(UUID id, String status) {
        return new CashSessionOutput(id, status, new BigDecimal("100.00"),
                null, LocalDateTime.of(2026, 1, 1, 8, 0), null, "admin", null, null);
    }

    private CashMovementOutput movementOutput(UUID id, UUID sessionId) {
        return new CashMovementOutput(id, sessionId, "INCOME", new BigDecimal("50.00"),
                "Sangria", "MANUAL", "admin", "MANUAL", LocalDateTime.of(2026, 1, 1, 9, 0));
    }

    private OrderOutput orderOutput(UUID id, String status) {
        return new OrderOutput(id, "Cliente Teste", null, "BALCAO", status,
                LocalDateTime.of(2026, 1, 1, 12, 0), null, null, null, null, null, null,
                new BigDecimal("29.90"), List.of());
    }

    private String json(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    @Test
    @DisplayName("GET /cashier/session/current: retorna 200 com sessão quando existe sessão aberta")
    void currentSession_returns200_whenSessionExists() throws Exception {
        UUID id = UUID.randomUUID();
        when(cashierService.getCurrentSession()).thenReturn(Optional.of(sessionOutput(id, "OPEN")));

        mvc.perform(get("/cashier/session/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(cashierService).getCurrentSession();
    }

    @Test
    @DisplayName("GET /cashier/session/current: retorna 204 quando não há sessão aberta")
    void currentSession_returns204_whenNoSession() throws Exception {
        when(cashierService.getCurrentSession()).thenReturn(Optional.empty());

        mvc.perform(get("/cashier/session/current"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /cashier/session/open: retorna 201 com sessão aberta")
    void openSession_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(cashierService.openSession(any(OpenSessionInput.class), anyString()))
                .thenReturn(sessionOutput(id, "OPEN"));

        OpenSessionInput input = new OpenSessionInput(new BigDecimal("100.00"), "Observação");

        mvc.perform(post("/cashier/session/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(cashierService).openSession(any(OpenSessionInput.class), anyString());
    }

    @Test
    @DisplayName("POST /cashier/session/open: retorna 409 quando já existe sessão aberta")
    void openSession_returns409_whenSessionAlreadyOpen() throws Exception {
        when(cashierService.openSession(any(OpenSessionInput.class), anyString()))
                .thenThrow(new OpenSessionAlreadyExistsException());

        OpenSessionInput input = new OpenSessionInput(new BigDecimal("100.00"), null);

        mvc.perform(post("/cashier/session/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /cashier/session/{id}/close: retorna 200 com sessão fechada")
    void closeSession_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(cashierService.closeSession(eq(id), any(CloseSessionInput.class), anyString()))
                .thenReturn(sessionOutput(id, "CLOSED"));

        CloseSessionInput input = new CloseSessionInput("Fechamento normal");

        mvc.perform(post("/cashier/session/{id}/close", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("POST /cashier/session/{id}/close: retorna 404 quando sessão não existe")
    void closeSession_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(cashierService.closeSession(eq(id), any(CloseSessionInput.class), anyString()))
                .thenThrow(new CashSessionNotFoundException(id));

        mvc.perform(post("/cashier/session/{id}/close", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CloseSessionInput(null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /cashier/sessions: retorna 200 com lista de sessões")
    void listSessions_returns200WithList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(cashierService.listSessions())
                .thenReturn(List.of(sessionOutput(id1, "CLOSED"), sessionOutput(id2, "OPEN")));

        mvc.perform(get("/cashier/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(cashierService).listSessions();
    }

    @Test
    @DisplayName("POST /cashier/session/{sessionId}/movement: retorna 201 com movimentação criada")
    void addMovement_returns201() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID movId = UUID.randomUUID();
        when(cashierService.addMovement(eq(sessionId), any(CashMovementInput.class), anyString(), anyString()))
                .thenReturn(movementOutput(movId, sessionId));

        CashMovementInput input = new CashMovementInput(
                TransactionType.INCOME, new BigDecimal("50.00"), "Suprimento", "MANUAL");

        mvc.perform(post("/cashier/session/{sessionId}/movement", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(movId.toString()))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @DisplayName("GET /cashier/session/{sessionId}/movements: retorna 200 com lista de movimentações")
    void listMovements_returns200WithList() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID movId = UUID.randomUUID();
        when(cashierService.listMovements(sessionId))
                .thenReturn(List.of(movementOutput(movId, sessionId)));

        mvc.perform(get("/cashier/session/{sessionId}/movements", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(cashierService).listMovements(sessionId);
    }

    @Test
    @DisplayName("GET /cashier/orders/pending: retorna 200 com pedidos pendentes de pagamento")
    void pendingOrders_returns200WithList() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.findByStatus("READY")).thenReturn(List.of(orderOutput(id, "READY")));

        mvc.perform(get("/cashier/orders/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("POST /cashier/orders/{id}/cancel: retorna 200 com pedido cancelado")
    void cancelOrder_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.cancel(eq(id), any(), any())).thenReturn(orderOutput(id, "CANCELADO"));

        mvc.perform(post("/cashier/orders/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        verify(orderService).cancel(eq(id), any(), any());
    }

    @Test
    @DisplayName("POST /cashier/orders/{id}/cancel: retorna 404 quando pedido não existe")
    void cancelOrder_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.cancel(eq(id), any(), any())).thenThrow(new OrderNotFoundException(id));

        mvc.perform(post("/cashier/orders/{id}/cancel", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /cashier/orders/{id}/settle: retorna 200 com pedido liquidado")
    void settleOrder_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.settleFromCashier(id, "PIX")).thenReturn(orderOutput(id, "COMPLETED"));

        SettleOrderInput input = new SettleOrderInput("PIX");

        mvc.perform(post("/cashier/orders/{id}/settle", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(orderService).settleFromCashier(id, "PIX");
    }
}
