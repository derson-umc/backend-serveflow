package com.serveflow.web.dto.product.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductOutput(
        UUID id,
        String name,
        String description,
        String category,
        String brand,
        BigDecimal price,
        String portion
) {}
