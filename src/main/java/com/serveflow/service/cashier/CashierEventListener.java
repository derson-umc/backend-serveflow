package com.serveflow.service.cashier;

import com.serveflow.controller.cashier.CashierEventPublisher;
import com.serveflow.dto.cashier.request.CashMovementInput;
import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.events.OrderCompletedEvent;
import com.serveflow.model.financial.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashierEventListener {

    private final CashierService         cashierService;
    private final CashierEventPublisher  eventPublisher;

    @Async
    @EventListener
    public void onOrderCompleted(OrderCompletedEvent event) {
        UUID sessionId = cashierService.getCurrentSessionId().orElse(null);
        if (sessionId == null) {
            log.info("OrderCompleted ignorado — caixa fechado. orderId={}", event.orderId());
            return;
        }

        String paymentMethod = event.paymentMethod() != null ? event.paymentMethod() : "NÃO INFORMADO";
        String description   = buildDescription(event);

        CashMovementInput input = new CashMovementInput(
                TransactionType.INCOME,
                event.total(),
                description,
                paymentMethod
        );

        try {
            CashMovementOutput output = cashierService.addMovement(sessionId, input, "sistema", "AUTOMATICO");
            eventPublisher.publishMovement(output);
            log.info("Entrada automática registrada: orderId={} valor={}", event.orderId(), event.total());
        } catch (Exception e) {
            log.error("Erro ao registrar entrada automática: orderId={} erro={}", event.orderId(), e.getMessage(), e);
        }
    }

    private String buildDescription(OrderCompletedEvent event) {
        String tipo = switch (event.orderType()) {
            case "DELIVERY"  -> "Delivery";
            case "MESA"      -> "Mesa";
            case "BALCAO"    -> "Balcão";
            default          -> event.orderType();
        };
        return "Pedido finalizado — %s · %s".formatted(tipo, event.customerName());
    }
}
