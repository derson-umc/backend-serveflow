package com.serveflow.dto.product.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductOutput(
        UUID id,
        String name,
        String description,
        String category,
        String brand,
        BigDecimal price,
        String portion,
        String imageUrl,
        boolean active,
        boolean requiresTechnicalSheet,
        LocalDateTime createdAt
) {}
