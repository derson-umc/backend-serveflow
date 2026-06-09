package com.serveflow.service.cashier;

import com.serveflow.dto.cashier.request.CashMovementInput;
import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.events.OrderCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashierEventListenerTest {

    @Mock
    CashierService cashierService;
    @Mock
    CashierEventPublisher eventPublisher;

    @InjectMocks
    CashierEventListener listener;

    private OrderCompletedEvent event(UUID orderId, String paymentMethod, String orderType) {
        return new OrderCompletedEvent(orderId, "Cliente Teste", orderType, paymentMethod,
                new BigDecimal("50.00"));
    }

    private CashMovementOutput movementOutput(UUID sessionId) {
        return new CashMovementOutput(UUID.randomUUID(), sessionId, "INCOME", new BigDecimal("50.00"),
                "desc", "PIX", "sistema", "AUTOMATICO", null);
    }

    @Nested
    @DisplayName("onOrderCompleted() — caixa aberto")
    class CaixaAberto {

        @Test
        @DisplayName("registra entrada automática e publica movimento quando caixa está aberto")
        void onOrderCompleted_registersMovement_whenSessionOpen() {
            UUID sessionId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            when(cashierService.getCurrentSessionId()).thenReturn(Optional.of(sessionId));
            when(cashierService.addMovement(eq(sessionId), any(CashMovementInput.class), eq("sistema"), eq("AUTOMATICO")))
                    .thenReturn(movementOutput(sessionId));

            listener.onOrderCompleted(event(orderId, "PIX", "BALCAO"));

            verify(cashierService).addMovement(eq(sessionId), any(CashMovementInput.class), eq("sistema"), eq("AUTOMATICO"));
            verify(eventPublisher).publishMovement(any(CashMovementOutput.class));
        }

        @Test
        @DisplayName("usa 'NÃO INFORMADO' quando paymentMethod é nulo")
        void onOrderCompleted_usesDefaultPayment_whenNull() {
            UUID sessionId = UUID.randomUUID();
            when(cashierService.getCurrentSessionId()).thenReturn(Optional.of(sessionId));
            ArgumentCaptor<CashMovementInput> captor = ArgumentCaptor.forClass(CashMovementInput.class);
            when(cashierService.addMovement(eq(sessionId), captor.capture(), anyString(), anyString()))
                    .thenReturn(movementOutput(sessionId));

            listener.onOrderCompleted(event(UUID.randomUUID(), null, "BALCAO"));

            assertThat(captor.getValue().category()).isEqualTo("NÃO INFORMADO");
        }

        @Test
        @DisplayName("descrição contém 'Balcão' para pedido BALCAO")
        void onOrderCompleted_descriptionContainsBalcao() {
            UUID sessionId = UUID.randomUUID();
            when(cashierService.getCurrentSessionId()).thenReturn(Optional.of(sessionId));
            ArgumentCaptor<CashMovementInput> captor = ArgumentCaptor.forClass(CashMovementInput.class);
            when(cashierService.addMovement(eq(sessionId), captor.capture(), anyString(), anyString()))
                    .thenReturn(movementOutput(sessionId));

            listener.onOrderCompleted(event(UUID.randomUUID(), "PIX", "BALCAO"));

            assertThat(captor.getValue().description()).contains("Balcão");
        }

        @Test
        @DisplayName("descrição contém 'Delivery' para pedido DELIVERY")
        void onOrderCompleted_descriptionContainsDelivery() {
            UUID sessionId = UUID.randomUUID();
            when(cashierService.getCurrentSessionId()).thenReturn(Optional.of(sessionId));
            ArgumentCaptor<CashMovementInput> captor = ArgumentCaptor.forClass(CashMovementInput.class);
            when(cashierService.addMovement(eq(sessionId), captor.capture(), anyString(), anyString()))
                    .thenReturn(movementOutput(sessionId));

            listener.onOrderCompleted(event(UUID.randomUUID(), "DINHEIRO", "DELIVERY"));

            assertThat(captor.getValue().description()).contains("Delivery");
        }

        @Test
        @DisplayName("descrição contém 'Local' para pedido MESA")
        void onOrderCompleted_descriptionContainsMesa() {
            UUID sessionId = UUID.randomUUID();
            when(cashierService.getCurrentSessionId()).thenReturn(Optional.of(sessionId));
            ArgumentCaptor<CashMovementInput> captor = ArgumentCaptor.forClass(CashMovementInput.class);
            when(cashierService.addMovement(eq(sessionId), captor.capture(), anyString(), anyString()))
                    .thenReturn(movementOutput(sessionId));

            listener.onOrderCompleted(event(UUID.randomUUID(), "CARTAO", "MESA"));

            assertThat(captor.getValue().description()).contains("Local");
        }

        @Test
        @DisplayName("não lança exceção quando addMovement falha")
        void onOrderCompleted_silentFailure_whenAddMovementThrows() {
            UUID sessionId = UUID.randomUUID();
            when(cashierService.getCurrentSessionId()).thenReturn(Optional.of(sessionId));
            when(cashierService.addMovement(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Caixa fechado"));

            // should not throw
            listener.onOrderCompleted(event(UUID.randomUUID(), "PIX", "BALCAO"));

            verify(eventPublisher, never()).publishMovement(any());
        }
    }

    @Nested
    @DisplayName("onOrderCompleted() — caixa fechado")
    class CaixaFechado {

        @Test
        @DisplayName("ignora evento quando não há sessão aberta")
        void onOrderCompleted_ignores_whenNoSession() {
            when(cashierService.getCurrentSessionId()).thenReturn(Optional.empty());

            listener.onOrderCompleted(event(UUID.randomUUID(), "PIX", "BALCAO"));

            verify(cashierService, never()).addMovement(any(), any(), any(), any());
            verify(eventPublisher, never()).publishMovement(any());
        }
    }
}
