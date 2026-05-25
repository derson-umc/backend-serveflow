package com.serveflow.controller.financial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.financial.request.AccountPayableInput;
import com.serveflow.dto.financial.request.AccountReceivableInput;
import com.serveflow.dto.financial.request.SettlementInput;
import com.serveflow.dto.financial.response.AccountPayableOutput;
import com.serveflow.dto.financial.response.AccountReceivableOutput;
import com.serveflow.dto.financial.response.CashFlowOutput;
import com.serveflow.exception.financial.AccountNotFoundException;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.financial.FinancialService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FinancialControllerTest {

    @Mock
    FinancialService financialService;
    @Mock
    AuditService auditService;

    @InjectMocks
    FinancialController controller;

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

    private AccountReceivableOutput receivableOutput(UUID id, String status) {
        return new AccountReceivableOutput(id, "Venda #001", LocalDate.of(2026, 6, 1),
                new BigDecimal("500.00"), status, null, null, "VENDAS", null,
                LocalDateTime.of(2026, 1, 1, 12, 0));
    }

    private AccountPayableOutput payableOutput(UUID id, String status) {
        return new AccountPayableOutput(id, "Fornecedor ABC", LocalDate.of(2026, 6, 1),
                new BigDecimal("150.00"), status, null, null, "COMPRAS", "Fornecedor X",
                LocalDateTime.of(2026, 1, 1, 12, 0));
    }

    private AccountReceivableInput validReceivableInput() {
        return new AccountReceivableInput("Venda #001", LocalDate.of(2030, 1, 1),
                new BigDecimal("500.00"), "VENDAS", null);
    }

    private AccountPayableInput validPayableInput() {
        return new AccountPayableInput("Fornecedor ABC", LocalDate.of(2030, 1, 1),
                new BigDecimal("150.00"), "COMPRAS", "Fornecedor X");
    }

    private SettlementInput settlementInput() {
        return new SettlementInput(new BigDecimal("500.00"), "admin");
    }

    private String json(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    @Test
    @DisplayName("GET /financial/cash-flow: retorna 200 com dados de fluxo de caixa")
    void cashFlow_returns200() throws Exception {
        CashFlowOutput output = new CashFlowOutput(
                new BigDecimal("500.00"), new BigDecimal("150.00"), new BigDecimal("350.00"),
                2, 1, new BigDecimal("200.00"), new BigDecimal("100.00"));
        when(financialService.calculateCashFlow()).thenReturn(output);

        mvc.perform(get("/financial/cash-flow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(500.00))
                .andExpect(jsonPath("$.balance").value(350.00));

        verify(financialService).calculateCashFlow();
    }

    @Test
    @DisplayName("POST /financial/receivables: retorna 201 com conta a receber criada")
    void createReceivable_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.createReceivable(any(AccountReceivableInput.class)))
                .thenReturn(receivableOutput(id, "PENDING"));

        mvc.perform(post("/financial/receivables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validReceivableInput())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(financialService).createReceivable(any(AccountReceivableInput.class));
    }

    @Test
    @DisplayName("POST /financial/receivables: retorna 400 quando campos obrigatórios ausentes")
    void createReceivable_returns400_whenInvalid() throws Exception {
        AccountReceivableInput invalid = new AccountReceivableInput(null, null, null, null, null);

        mvc.perform(post("/financial/receivables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /financial/receivables: retorna 200 com lista de contas a receber")
    void listReceivables_returns200WithList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(financialService.listReceivables())
                .thenReturn(List.of(receivableOutput(id1, "PENDING"), receivableOutput(id2, "RECEIVED")));

        mvc.perform(get("/financial/receivables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(financialService).listReceivables();
    }

    @Test
    @DisplayName("GET /financial/receivables/{id}: retorna 200 quando conta existe")
    void findReceivable_returns200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.findReceivable(id)).thenReturn(receivableOutput(id, "PENDING"));

        mvc.perform(get("/financial/receivables/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(financialService).findReceivable(id);
    }

    @Test
    @DisplayName("GET /financial/receivables/{id}: retorna 404 quando conta não existe")
    void findReceivable_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.findReceivable(id))
                .thenThrow(new AccountNotFoundException(id));

        mvc.perform(get("/financial/receivables/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /financial/receivables/{id}/settle: retorna 200 com conta liquidada")
    void settleReceivable_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.settleReceivable(eq(id), any(SettlementInput.class)))
                .thenReturn(receivableOutput(id, "RECEIVED"));

        mvc.perform(patch("/financial/receivables/{id}/settle", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(settlementInput())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        verify(financialService).settleReceivable(eq(id), any(SettlementInput.class));
    }

    @Test
    @DisplayName("PATCH /financial/receivables/{id}/cancel: retorna 200 com conta cancelada")
    void cancelReceivable_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.cancelReceivable(eq(id), anyString()))
                .thenReturn(receivableOutput(id, "CANCELLED"));

        mvc.perform(patch("/financial/receivables/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(financialService).cancelReceivable(eq(id), anyString());
    }

    @Test
    @DisplayName("POST /financial/payables: retorna 201 com conta a pagar criada")
    void createPayable_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.createPayable(any(AccountPayableInput.class)))
                .thenReturn(payableOutput(id, "PENDING"));

        mvc.perform(post("/financial/payables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validPayableInput())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(financialService).createPayable(any(AccountPayableInput.class));
    }

    @Test
    @DisplayName("GET /financial/payables: retorna 200 com lista de contas a pagar")
    void listPayables_returns200WithList() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.listPayables()).thenReturn(List.of(payableOutput(id, "PENDING")));

        mvc.perform(get("/financial/payables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(financialService).listPayables();
    }

    @Test
    @DisplayName("GET /financial/payables/{id}: retorna 200 quando conta existe")
    void findPayable_returns200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.findPayable(id)).thenReturn(payableOutput(id, "PENDING"));

        mvc.perform(get("/financial/payables/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(financialService).findPayable(id);
    }

    @Test
    @DisplayName("GET /financial/payables/{id}: retorna 404 quando conta não existe")
    void findPayable_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.findPayable(id))
                .thenThrow(new AccountNotFoundException(id));

        mvc.perform(get("/financial/payables/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /financial/payables/{id}/settle: retorna 200 com conta paga")
    void settlePayable_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.settlePayable(eq(id), any(SettlementInput.class)))
                .thenReturn(payableOutput(id, "PAID"));

        mvc.perform(patch("/financial/payables/{id}/settle", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(settlementInput())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(financialService).settlePayable(eq(id), any(SettlementInput.class));
    }

    @Test
    @DisplayName("PATCH /financial/payables/{id}/cancel: retorna 200 com conta cancelada")
    void cancelPayable_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(financialService.cancelPayable(eq(id), anyString()))
                .thenReturn(payableOutput(id, "CANCELLED"));

        mvc.perform(patch("/financial/payables/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(financialService).cancelPayable(eq(id), anyString());
    }
}
