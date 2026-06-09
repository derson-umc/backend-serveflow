package com.serveflow.controller.financial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.repository.cashier.SpringCashMovementRepository;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.financial.FinancialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FinancialControllerExtendedTest {

    @Mock FinancialService financialService;
    @Mock AuditService auditService;
    @Mock SpringCashMovementRepository movementRepository;

    @InjectMocks FinancialController controller;

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

    @Nested
    @DisplayName("GET /financial/cashier-report")
    class CashierReport {

        @Test
        @DisplayName("retorna 200 com relatório de caixa vazio quando sem movimentos")
        void cashierReport_returns200_withEmptyData() throws Exception {
            when(movementRepository.reportByPaymentMethod(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.<Object[]>of());

            mvc.perform(get("/financial/cashier-report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.byPaymentMethod").isArray())
                    .andExpect(jsonPath("$.byPaymentMethod.length()").value(0))
                    .andExpect(jsonPath("$.grossTotal").value(0));
        }

        @Test
        @DisplayName("retorna 200 com relatório de caixa com dados reais")
        void cashierReport_returns200_withData() throws Exception {
            // Object[] = [method, count, total]
            List<Object[]> rows = List.of(
                    new Object[]{"PIX", 5L, new BigDecimal("250.00")},
                    new Object[]{"DINHEIRO", 3L, new BigDecimal("120.00")}
            );
            when(movementRepository.reportByPaymentMethod(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(rows);

            mvc.perform(get("/financial/cashier-report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.byPaymentMethod.length()").value(2))
                    .andExpect(jsonPath("$.grossTotal").value(370.00));
        }

        @Test
        @DisplayName("retorna 200 com datas customizadas")
        void cashierReport_withCustomDates_returns200() throws Exception {
            when(movementRepository.reportByPaymentMethod(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.<Object[]>of());

            mvc.perform(get("/financial/cashier-report")
                            .param("startDate", "2026-01-01")
                            .param("endDate", "2026-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startDate").isNotEmpty())
                    .andExpect(jsonPath("$.endDate").isNotEmpty());
        }

        @Test
        @DisplayName("retorna 200 quando row[0] é null (método de pagamento null)")
        void cashierReport_returns200_whenMethodNull() throws Exception {
            java.util.ArrayList<Object[]> rows = new java.util.ArrayList<>();
            rows.add(new Object[]{null, 2L, new BigDecimal("80.00")});
            when(movementRepository.reportByPaymentMethod(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(rows);

            mvc.perform(get("/financial/cashier-report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.byPaymentMethod[0].method").value("SEM_METODO"))
                    .andExpect(jsonPath("$.byPaymentMethod[0].total").value(80.00));
        }
    }
}
