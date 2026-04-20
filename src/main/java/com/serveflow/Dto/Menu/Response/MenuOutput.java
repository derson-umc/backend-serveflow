package com.serveflow.Dto.Menu.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MenuOutput(
        UUID id,
        String name,
        String status,
        UUID activeOrderId,
        List<MenuItemOutput> items,
        LocalDateTime createdAt
) {
    public record MenuItemOutput(
            UUID id,
            UUID productId,
            String name,
            String description,
            BigDecimal price,
            boolean available,
            boolean removed,
            String removedBy
    ) {}
}
