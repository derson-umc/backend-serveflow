package com.serveflow.model.order;

import java.math.BigDecimal;
import java.util.*;

public class OrderItem {

    private final UUID id;
    private final UUID productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final String observation;
    private final List<ItemAdditional> additionals;
    private final String productCategory;

    private OrderItemStatus status;
    private String cancelReason;

    public OrderItem(UUID id, UUID productId, String productName, int quantity,
                     BigDecimal unitPrice, String observation,
                     List<ItemAdditional> additionals,
                     OrderItemStatus status, String cancelReason,
                     String productCategory) {

        this.id = Objects.requireNonNull(id, "ID do item é obrigatório.");
        this.productId = Objects.requireNonNull(productId, "ID do produto é obrigatório.");
        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("Nome do produto é obrigatório.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço unitário deve ser maior que zero.");

        this.productName = productName.strip();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.observation = observation != null ? observation.strip() : null;
        this.additionals = new ArrayList<>(Optional.ofNullable(additionals).orElse(List.of()));
        this.status = status != null ? status : OrderItemStatus.PENDENTE;
        this.cancelReason = cancelReason;
        this.productCategory = productCategory;
    }

    public OrderItem(UUID productId, String productName, int quantity,
                     BigDecimal unitPrice, String observation,
                     List<ItemAdditional> additionals) {
        this(UUID.randomUUID(), productId, productName, quantity, unitPrice, observation, additionals,
                OrderItemStatus.PENDENTE, null, null);
    }

    public OrderItem(UUID productId, String productName, int quantity,
                     BigDecimal unitPrice, String observation,
                     List<ItemAdditional> additionals, String productCategory) {
        this(UUID.randomUUID(), productId, productName, quantity, unitPrice, observation, additionals,
                OrderItemStatus.PENDENTE, null, productCategory);
    }

    public void syncStatus(OrderItemStatus newStatus) {
        this.status = newStatus;
    }

    public void cancel(OrderItemStatus cancelStatus, String reason) {
        this.status = cancelStatus;
        this.cancelReason = reason;
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

    public UUID getId()                     { return id; }
    public UUID getProductId()              { return productId; }
    public String getProductName()          { return productName; }
    public int getQuantity()                { return quantity; }
    public BigDecimal getUnitPrice()        { return unitPrice; }
    public String getObservation()          { return observation; }
    public List<ItemAdditional> getAdditionals() { return List.copyOf(additionals); }
    public OrderItemStatus getStatus()      { return status; }
    public String getCancelReason()         { return cancelReason; }
    public String getProductCategory()      { return productCategory; }
}
