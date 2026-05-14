package com.serveflow.model.order;

import java.math.BigDecimal;
import java.util.*;

public class OrderItem {

    private UUID id;
    private final UUID productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final String observation;
    private final List<ItemAdditional> additionals;

    public OrderItem(UUID productId, String productName, int quantity,
                     BigDecimal unitPrice, String observation,
                     List<ItemAdditional> additionals) {

        this.productId = Objects.requireNonNull(productId, "ID do produto é obrigatório.");
        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero.");

        this.id = UUID.randomUUID();
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

    public UUID getId()              { return id; }
    public UUID getProductId()       { return productId; }
    public String getProductName()   { return productName; }
    public int getQuantity()         { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getObservation()   { return observation; }
    public List<ItemAdditional> getAdditionals() { return List.copyOf(additionals); }
}
