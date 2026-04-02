package com.serveflow.web.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        String description,
        String category,
        String brand,
        BigDecimal price,
        String portion
) {}
