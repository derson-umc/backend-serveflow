package com.serveflow.Model.Product;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
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

    private Long version;

    public void deactivate() { this.active = false; }
    public void activate()   { this.active = true; }

    public void update(Product other) {
        if (other.name != null)        this.name        = other.name;
        if (other.description != null) this.description = other.description;
        if (other.category != null)    this.category    = other.category;
        if (other.brand != null)       this.brand       = other.brand;
        if (other.price != null)       this.price       = other.price;
        if (other.portion != null)     this.portion     = other.portion;
    }
}
