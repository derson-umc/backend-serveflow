package com.serveflow.web.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID id,
        String customerName,
        AddressResponseDTO address,
        String type,
        String status,
        LocalDateTime createdAt,
        String observation,
        BigDecimal totalValue,
        List<OrderItemResponseDTO> items
) {
    public record AddressResponseDTO(
            UUID id,
            String cep,
            String street,
            String city,
            String state,
            String number,
            String complement
    ) {}

    public record OrderItemResponseDTO(
            UUID id,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            String observation,
            BigDecimal total,
            List<ItemAdditionalResponseDTO> additionals
    ) {}

    public record ItemAdditionalResponseDTO(
            UUID id,
            String name,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal total
    ) {}
}
