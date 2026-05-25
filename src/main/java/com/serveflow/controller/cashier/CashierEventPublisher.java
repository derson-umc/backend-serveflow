package com.serveflow.controller.cashier;

import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.dto.cashier.response.CashSessionOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashierEventPublisher {

    private static final String MOVEMENTS_TOPIC = "/topic/cashier/movements";
    private static final String SESSIONS_TOPIC  = "/topic/cashier/sessions";

    private final SimpMessagingTemplate broker;

    public void publishMovement(CashMovementOutput movement) {
        broker.convertAndSend(MOVEMENTS_TOPIC, new CashierMovementEvent("NEW_MOVEMENT", movement));
    }

    public void publishSessionOpened(CashSessionOutput session) {
        broker.convertAndSend(SESSIONS_TOPIC, new CashierSessionEvent("OPENED", session));
    }

    public void publishSessionClosed(CashSessionOutput session) {
        broker.convertAndSend(SESSIONS_TOPIC, new CashierSessionEvent("CLOSED", session));
    }

    public record CashierMovementEvent(String type, CashMovementOutput movement) {}
    public record CashierSessionEvent(String type, CashSessionOutput session) {}
}
