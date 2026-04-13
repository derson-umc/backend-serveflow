package com.serveflow.web.dto.menu.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MenuOutPut(
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
