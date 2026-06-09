package com.serveflow.service.cashier;

import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.dto.cashier.response.CashSessionOutput;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;


@Component
public class CashierEventPublisher {

    public static final String TOPIC = "/topic/caixa";

    private final SimpMessagingTemplate broker;

    public CashierEventPublisher(SimpMessagingTemplate broker) {
        this.broker = broker;
    }

    public void publishSessionOpened(CashSessionOutput output) {
        broker.convertAndSend(TOPIC, new CashSessionEvent("SESSION_OPENED", output));
    }

    public void publishSessionClosed(CashSessionOutput output) {
        broker.convertAndSend(TOPIC, new CashSessionEvent("SESSION_CLOSED", output));
    }

    public void publishMovement(CashMovementOutput output) {
        broker.convertAndSend(TOPIC, new CashMovementEvent("CASH_MOVEMENT", output));
    }

    public void publishBillCloseRequested(UUID orderId, String tableId, BigDecimal totalAmount) {
        broker.convertAndSend(TOPIC, new BillCloseEvent(
                "BILL_CLOSE_REQUESTED",
                orderId,
                tableId,
                totalAmount
        ));
    }

    public record BillCloseEvent(
            String     event,
            UUID       orderSessionId,
            String     tableId,
            BigDecimal totalAmount
    ) {}

    public record CashMovementEvent(
            String             event,
            CashMovementOutput movement
    ) {}

    public record CashSessionEvent(
            String            event,
            CashSessionOutput session
    ) {}
}
