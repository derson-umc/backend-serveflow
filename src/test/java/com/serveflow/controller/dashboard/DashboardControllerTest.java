package com.serveflow.controller.dashboard;

import com.serveflow.repository.dashboard.DashboardReadRepository;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.dashboard.GetDashboardMetricsUseCase;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    GetDashboardMetricsUseCase metricsUseCase;

    @Mock
    DashboardReadRepository readRepository;

    @Mock
    AuditService auditService;

    @InjectMocks
    DashboardController controller;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(auditService))
                .build();
    }

    @Nested
    @DisplayName("GET /dashboard/metrics")
    class Metrics {

        @Test
        @DisplayName("retorna métricas de hoje e ontem com 200")
        void metrics_returns200() throws Exception {
            GetDashboardMetricsUseCase.Output output = new GetDashboardMetricsUseCase.Output(
                    new BigDecimal("500.00"), 10, 8, new BigDecimal("62.50"),
                    new BigDecimal("300.00"), 6, 5, new BigDecimal("60.00")
            );
            when(metricsUseCase.execute()).thenReturn(output);

            mvc.perform(get("/dashboard/metrics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.revenueToday").value(500.00))
                    .andExpect(jsonPath("$.ordersToday").value(10))
                    .andExpect(jsonPath("$.customersToday").value(8))
                    .andExpect(jsonPath("$.ticketMedio").value(62.50));
        }
    }

    @Nested
    @DisplayName("GET /dashboard/sales-by-day")
    class SalesByDay {

        @Test
        @DisplayName("retorna lista de vendas por dia com 200")
        void salesByDay_returns200() throws Exception {
            String today = LocalDate.now().toString();
            Object[] row = { today, "200.00" };
            List<Object[]> rows = new java.util.ArrayList<>();
            rows.add(row);
            when(readRepository.salesByDay()).thenReturn(rows);

            mvc.perform(get("/dashboard/sales-by-day"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].total").value(200.00));
        }

        @Test
        @DisplayName("retorna lista vazia quando não há dados")
        void salesByDay_emptyList_returns200() throws Exception {
            when(readRepository.salesByDay()).thenReturn(List.of());

            mvc.perform(get("/dashboard/sales-by-day"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /dashboard/top-products")
    class TopProducts {

        @Test
        @DisplayName("retorna top produtos com 200 e parâmetro padrão de 30 dias")
        void topProducts_returnsDefaultPeriod() throws Exception {
            Object[] row = { "X-Burguer", 15, "750.00" };
            List<Object[]> rows = new java.util.ArrayList<>();
            rows.add(row);
            when(readRepository.topProductsByPeriod(any(LocalDate.class)))
                    .thenReturn(rows);

            mvc.perform(get("/dashboard/top-products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("X-Burguer"))
                    .andExpect(jsonPath("$[0].quantity").value(15));
        }

        @Test
        @DisplayName("retorna top produtos com período customizado")
        void topProducts_withCustomDays() throws Exception {
            when(readRepository.topProductsByPeriod(any(LocalDate.class)))
                    .thenReturn(List.of());

            mvc.perform(get("/dashboard/top-products").param("days", "7"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /dashboard/cashier-report")
    class CashierReport {

        @Test
        @DisplayName("retorna relatório do caixa com datas padrão (hoje)")
        void cashierReport_defaultDates() throws Exception {
            Object[] row = { "PIX", 5, "500.00" };
            List<Object[]> rows = new java.util.ArrayList<>();
            rows.add(row);
            when(readRepository.cashierReportByPayment(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(rows);

            mvc.perform(get("/dashboard/cashier-report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.byPaymentMethod[0].method").value("PIX"))
                    .andExpect(jsonPath("$.byPaymentMethod[0].ordersCount").value(5))
                    .andExpect(jsonPath("$.grossTotal").value(500.00));
        }

        @Test
        @DisplayName("retorna relatório do caixa com datas customizadas")
        void cashierReport_customDates() throws Exception {
            when(readRepository.cashierReportByPayment(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of());

            mvc.perform(get("/dashboard/cashier-report")
                            .param("startDate", "2025-01-01")
                            .param("endDate", "2025-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.grossTotal").value(0.00));
        }
    }
}
