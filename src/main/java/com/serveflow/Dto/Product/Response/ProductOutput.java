package com.serveflow.Dto.Product.Response;

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
        boolean active,
        LocalDateTime createdAt
) {}
