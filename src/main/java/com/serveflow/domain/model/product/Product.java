package com.serveflow.domain.model.product;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {

    @EqualsAndHashCode.Include
    private UUID id;

    private String name;
    private String description;
    private String category;
    private String brand;
    private BigDecimal price;
    private String portion;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}