package com.serveflow.domain.model.order;

import java.math.BigDecimal;
import java.util.*;

public class OrderItem {

    private UUID id;
    private final UUID productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final String observation;
    private final List<com.serveflow.domain.model.order.ItemAdditional> additionals;

    public OrderItem(UUID productId, String productName, int quantity,
                     BigDecimal unitPrice, String observation,
                     List<com.serveflow.domain.model.order.ItemAdditional> additionals) {

        if (productId == null)
            throw new IllegalArgumentException("Produto e obrigatorio.");
        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("Nome do produto e obrigatorio.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preco unitario deve ser maior que zero.");

        this.productId = productId;
        this.productName = productName.strip();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.observation = observation != null ? observation.strip() : null;
        this.additionals = new ArrayList<>(Optional.ofNullable(additionals).orElse(List.of()));
    }

    public OrderItem(UUID id, UUID productId, String productName, int quantity,
                     BigDecimal unitPrice, String observation,
                     List<ItemAdditional> additionals) {
        this(productId, productName, quantity, unitPrice, observation, additionals);
        this.id = id;
    }

    public BigDecimal getItemPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getAdditionalsPrice() {
        return additionals.stream()
                .map(ItemAdditional::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getItemPrice().add(getAdditionalsPrice());
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getObservation() { return observation; }
    public List<ItemAdditional> getAdditionals() { return List.copyOf(additionals); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem other)) return false;
        return Objects.equals(productId, other.productId);
    }

    @Override
    public int hashCode() { return Objects.hash(productId); }
}
