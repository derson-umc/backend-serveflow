package com.serveflow.domain.model.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Product {

    private UUID id;
    private String name;
    private String description;
    private String category;
    private String brand;
    private BigDecimal price;
    private String portion;
    private boolean active;
    private LocalDateTime createdAt;

    public Product(String name, String description, String category,
                   String brand, BigDecimal price, String portion) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.portion = portion;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Product(UUID id, String name, String description, String category,
                   String brand, BigDecimal price, String portion,
                   boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.portion = portion;
        this.active = active;
        this.createdAt = createdAt;
    }

    public void update(String name, String description, String category,
                       String brand, BigDecimal price, String portion) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.portion = portion;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getPortion() {
        return portion;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
