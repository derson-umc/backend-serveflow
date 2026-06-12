package com.serveflow.service.dashboard;

import com.serveflow.repository.dashboard.DashboardReadRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDashboardMetricsUseCaseTest {

    @Mock
    DashboardReadRepository repo;

    @InjectMocks
    GetDashboardMetricsUseCase useCase;

    @Nested
    @DisplayName("execute()")
    class Execute {

        @Test
        @DisplayName("calcula ticketMedio corretamente dividindo por comandas")
        void execute_calculatesTicketMedio_whenOrdersPresent() {
            when(repo.revenueToday()).thenReturn(new BigDecimal("300.00"));
            when(repo.ordersToday()).thenReturn(5L);
            when(repo.customersToday()).thenReturn(3L);
            when(repo.revenueYesterday()).thenReturn(new BigDecimal("150.00"));
            when(repo.ordersYesterday()).thenReturn(2L);
            when(repo.customersYesterday()).thenReturn(2L);

            GetDashboardMetricsUseCase.Output output = useCase.execute();

            assertThat(output.revenueToday()).isEqualByComparingTo("300.00");
            assertThat(output.ordersToday()).isEqualTo(5);
            assertThat(output.customersToday()).isEqualTo(3);
            assertThat(output.ticketMedio()).isEqualByComparingTo("60.00");
            assertThat(output.revenueYesterday()).isEqualByComparingTo("150.00");
            assertThat(output.ordersYesterday()).isEqualTo(2);
            assertThat(output.customersYesterday()).isEqualTo(2);
            assertThat(output.ticketMedioYesterday()).isEqualByComparingTo("75.00");
        }

        @Test
        @DisplayName("retorna ticketMedio ZERO quando não há comandas hoje")
        void execute_returnsZeroTicketMedio_whenNoOrdersToday() {
            when(repo.revenueToday()).thenReturn(BigDecimal.ZERO);
            when(repo.ordersToday()).thenReturn(0L);
            when(repo.customersToday()).thenReturn(0L);
            when(repo.revenueYesterday()).thenReturn(BigDecimal.ZERO);
            when(repo.ordersYesterday()).thenReturn(0L);
            when(repo.customersYesterday()).thenReturn(0L);

            GetDashboardMetricsUseCase.Output output = useCase.execute();

            assertThat(output.ticketMedio()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(output.ticketMedioYesterday()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("retorna ticketMedioYesterday ZERO quando não há comandas ontem")
        void execute_returnsZeroTicketMedioYesterday_whenNoOrdersYesterday() {
            when(repo.revenueToday()).thenReturn(new BigDecimal("200.00"));
            when(repo.ordersToday()).thenReturn(2L);
            when(repo.customersToday()).thenReturn(2L);
            when(repo.revenueYesterday()).thenReturn(BigDecimal.ZERO);
            when(repo.ordersYesterday()).thenReturn(0L);
            when(repo.customersYesterday()).thenReturn(0L);

            GetDashboardMetricsUseCase.Output output = useCase.execute();

            assertThat(output.ticketMedio()).isEqualByComparingTo("100.00");
            assertThat(output.ticketMedioYesterday()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("arredonda ticketMedio com HALF_UP para 2 casas decimais")
        void execute_roundsTicketMedio_halfUp() {
            when(repo.revenueToday()).thenReturn(new BigDecimal("100.00"));
            when(repo.ordersToday()).thenReturn(3L);
            when(repo.customersToday()).thenReturn(3L);
            when(repo.revenueYesterday()).thenReturn(BigDecimal.ZERO);
            when(repo.ordersYesterday()).thenReturn(0L);
            when(repo.customersYesterday()).thenReturn(0L);

            GetDashboardMetricsUseCase.Output output = useCase.execute();

            // 100/3 = 33.33...
            assertThat(output.ticketMedio()).isEqualByComparingTo("33.33");
        }
    }
}
