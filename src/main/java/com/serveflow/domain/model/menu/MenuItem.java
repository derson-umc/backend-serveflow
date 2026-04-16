package com.serveflow.domain.model.menu;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class MenuItem {

    private final UUID id;
    private final UUID productId;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available;

    public MenuItem(UUID id, UUID productId, String name, String description,
                    BigDecimal price, boolean available) {
        this.id = Objects.requireNonNull(id, "ID do item do menu e obrigatório.");
        this.productId = Objects.requireNonNull(productId, "ID do produto e obrigatório.");
        setName(name);
        this.description = description;
        setPrice(price);
        this.available = available;
    }

    public static MenuItem create(UUID productId, String name, String description, BigDecimal price) {
        return new MenuItem(UUID.randomUUID(), productId, name, description, price, true);
    }

    public void updateAvailability(boolean hasStock) {
        this.available = hasStock;
    }

    public void update(String name, String description, BigDecimal price) {
        setName(name);
        this.description = description;
        setPrice(price);
    }

    public boolean isAvailable() {
        return available;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItem other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome do item do menu e obrigatório.");
        this.name = name.strip();
    }

    private void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço deve ser maior que zero.");
        this.price = price;
    }
}
