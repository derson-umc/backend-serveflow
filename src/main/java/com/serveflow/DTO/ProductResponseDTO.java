package com.serveflow.DTO;

import com.serveflow.Model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        String name,
        String description,
        String category,
        String brand,
        BigDecimal price,
        boolean active,
        String portion,
        LocalDateTime createdAt
) {
    // Factory method a partir da entidade
    public static ProductResponseDTO fromEntity(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBrand(),
                product.getPrice(),
                product.isActive(),
                product.getPortion(),
                product.getCreatedAt()
        );
    }
}
