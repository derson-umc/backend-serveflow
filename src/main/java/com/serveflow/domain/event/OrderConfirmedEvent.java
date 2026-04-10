package com.serveflow.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderConfirmedEvent(
        UUID orderId,
        List<OrderItemSnapshot> items,
        LocalDateTime occurredOn
) implements DomainEvent {

    public record OrderItemSnapshot(UUID productId, String productName, int quantity) {}
}
