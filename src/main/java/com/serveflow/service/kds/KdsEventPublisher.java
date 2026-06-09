package com.serveflow.service.kds;

import com.serveflow.dto.kds.response.KdsOrderOutput;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KdsEventPublisher {

    private static final String TOPIC = "/topic/kds/orders";

    private final SimpMessagingTemplate broker;

    public KdsEventPublisher(SimpMessagingTemplate broker) {
        this.broker = broker;
    }

    public void publishUpdate(KdsOrderOutput order) {
        broker.convertAndSend(TOPIC, new KdsEvent("UPDATE", order.id(), order, null));
    }

    public void publishRemove(UUID orderId, String finalStatus) {
        broker.convertAndSend(TOPIC, new KdsEvent("REMOVE", orderId, null, finalStatus));
    }

    @Deprecated
    public void publishRemove(UUID orderId) {
        broker.convertAndSend(TOPIC, new KdsEvent("REMOVE", orderId, null, null));
    }

    public record KdsEvent(String type, UUID orderId, KdsOrderOutput order, String finalStatus) {}
}
