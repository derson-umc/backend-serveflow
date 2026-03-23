package com.serveflow.Model;

import jakarta.persistence.*;
import lombok.*;
import com.serveflow.DTO.ProductRequestDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_product", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "brand", nullable = false, length = 80)
    private String brand;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "portion", nullable = false, length = 50)
    private String portion;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    // Factory method a partir Request
    public static Product fromRequest(ProductRequestDTO request) {
        Product product = new Product();
        product.name = request.name();
        product.description = request.description();
        product.category = request.category();
        product.brand = request.brand();
        product.price = request.price();
        product.portion = request.portion();
        return product;
    }

    // Métodos de negócio
    public void updateFrom(ProductRequestDTO request) {
        this.name = request.name();
        this.description = request.description();
        this.category = request.category();
        this.brand = request.brand();
        this.price = request.price();
        this.portion = request.portion();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}