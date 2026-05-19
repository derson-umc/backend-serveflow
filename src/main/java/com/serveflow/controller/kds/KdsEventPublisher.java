package com.serveflow.controller.kds;

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

    public void publishUpdate(KdsController.KdsOrderOutput order) {
        broker.convertAndSend(TOPIC, new KdsEvent("UPDATE", order.id(), order));
    }

    public void publishRemove(UUID orderId) {
        broker.convertAndSend(TOPIC, new KdsEvent("REMOVE", orderId, null));
    }

    public record KdsEvent(String type, UUID orderId, KdsController.KdsOrderOutput order) {}
}
