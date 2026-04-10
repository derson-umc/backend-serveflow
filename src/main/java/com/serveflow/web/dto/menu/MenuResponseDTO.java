package com.serveflow.web.dto.menu;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MenuResponseDTO(
    UUID id,
    String name,
    String status,
    UUID activeOrderId,
    List<MenuItemResponseDTO> items,
    LocalDateTime createdAt
) {
    public record MenuItemResponseDTO(
        UUID id,
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        boolean available
    ) {}
}
