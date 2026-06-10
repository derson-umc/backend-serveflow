package com.serveflow.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCompletedEvent(
        UUID    orderId,
        String  customerName,
        String  orderType,
        String  paymentMethod,
        BigDecimal total,
        String  settledBy
) {}
