package com.serveflow.model.order;

import com.serveflow.model.address.Address;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class Order {

    private final UUID id;
    private final String customerName;
    private final Address address;
    private final OrderType type;
    private final LocalDateTime createdAt;
    private final String observation;
    private final String tableNumber;
    private final List<OrderItem> items;

    private OrderStatus status;
    private String paymentMethod;
    private String cancelReason;
    private String canceledBy;
    private LocalDateTime canceledAt;
    private Long version;

    public static Order create(String customerName, Address address,
                               OrderType type, String observation, String tableNumber) {
        validateCustomerName(customerName);
        validateDeliveryAddress(type, address);
        validateTableNumber(type, tableNumber);

        return new Order(
                UUID.randomUUID(),
                customerName.strip(),
                type == OrderType.DELIVERY ? address : null,
                type,
                OrderStatus.RASCUNHO,
                LocalDateTime.now(),
                observation,
                null,
                tableNumber,
                null,
                null,
                null,
                new ArrayList<>(),
                null
        );
    }

    public Order(UUID id, String customerName, Address address, OrderType type,
                 OrderStatus status, LocalDateTime createdAt, String observation,
                 String paymentMethod, String tableNumber,
                 String cancelReason, String canceledBy, LocalDateTime canceledAt,
                 List<OrderItem> items, Long version) {

        this.id = Objects.requireNonNull(id, "ID do pedido é obrigatório.");
        this.customerName = Objects.requireNonNull(customerName, "Nome do cliente é obrigatório.");
        this.address = address;
        this.type = Objects.requireNonNull(type, "Tipo do pedido é obrigatório.");
        this.status = Objects.requireNonNull(status, "Status do pedido é obrigatório.");
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação é obrigatória.");
        this.observation = observation != null ? observation.strip() : null;
        this.paymentMethod = paymentMethod;
        this.tableNumber = tableNumber;
        this.cancelReason = cancelReason;
        this.canceledBy = canceledBy;
        this.canceledAt = canceledAt;
        this.items = new ArrayList<>(Optional.ofNullable(items).orElse(List.of()));
        this.version = version;
    }

    public void addItem(OrderItem item) {
        ensureModifiable();
        Objects.requireNonNull(item, "Item não pode ser nulo.");
        items.add(item);
    }

    public void removeItem(OrderItem item) {
        ensureModifiable();
        if (items.size() <= 1)
            throw new IllegalStateException("Pedido deve conter ao menos um item.");
        if (!items.remove(item))
            throw new IllegalArgumentException("Item não encontrado no pedido.");
    }

    public void confirm() {
        ensureHasItems();
        transitionTo(OrderStatus.ENVIADO);
        items.forEach(item -> item.syncStatus(OrderItemStatus.ENVIADO));
    }

    public void startPreparation() {
        transitionTo(OrderStatus.EM_PREPARO);
        items.forEach(item -> item.syncStatus(OrderItemStatus.EM_PREPARO));
    }

    public void markReady() {
        transitionTo(OrderStatus.PRONTO);
        items.forEach(item -> item.syncStatus(OrderItemStatus.PRONTO));
    }

    public void sendForDelivery() {
        if (type != OrderType.DELIVERY)
            throw new IllegalStateException("Apenas pedidos delivery podem ser enviados para entrega.");
        transitionTo(OrderStatus.A_CAMINHO);
    }

    public void complete() { transitionTo(OrderStatus.ENTREGUE); }

    public void cancel(String reason, String canceledBy) {
        OrderItemStatus itemCancelStatus = status == OrderStatus.EM_PREPARO
                ? OrderItemStatus.CANCELADO_EM_PREPARO
                : OrderItemStatus.CANCELADO_ANTES_PREPARO;

        items.forEach(item -> {
            if (item.getStatus() != OrderItemStatus.CANCELADO_ANTES_PREPARO
                    && item.getStatus() != OrderItemStatus.CANCELADO_EM_PREPARO) {
                item.cancel(itemCancelStatus, reason);
            }
        });

        this.cancelReason = reason;
        this.canceledBy = canceledBy;
        this.canceledAt = LocalDateTime.now();
        transitionTo(OrderStatus.CANCELADO);
    }

    public void registerPayment(String method) {
        if (method == null || method.isBlank())
            throw new IllegalArgumentException("Método de pagamento é obrigatório.");
        this.paymentMethod = method.strip().toUpperCase();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isFinalized() { return status.isFinal(); }
    public boolean isDelivery()  { return type == OrderType.DELIVERY; }
    public int getItemCount()    { return items.size(); }

    public UUID getId()                  { return id; }
    public String getCustomerName()      { return customerName; }
    public Address getAddress()          { return address; }
    public OrderType getType()           { return type; }
    public OrderStatus getStatus()       { return status; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public String getObservation()       { return observation; }
    public String getPaymentMethod()     { return paymentMethod; }
    public String getTableNumber()       { return tableNumber; }
    public String getCancelReason()      { return cancelReason; }
    public String getCanceledBy()        { return canceledBy; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public List<OrderItem> getItems()    { return List.copyOf(items); }
    public Long getVersion()             { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    private void transitionTo(OrderStatus target) {
        if (!status.canTransitionTo(target))
            throw new IllegalStateException(
                    "Transição inválida: " + status.getDescription() + " → " + target.getDescription());
        this.status = target;
    }

    private void ensureModifiable() {
        if (status.isFinal())
            throw new IllegalStateException("Pedido finalizado não pode ser alterado.");
    }

    private void ensureHasItems() {
        if (items.isEmpty())
            throw new IllegalStateException("Pedido deve conter ao menos um item para ser confirmado.");
    }

    private static void validateCustomerName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome do cliente é obrigatório.");
    }

    private static void validateDeliveryAddress(OrderType type, Address address) {
        if (type == OrderType.DELIVERY && address == null)
            throw new IllegalArgumentException("Endereço é obrigatório para pedidos delivery.");
    }

    private static void validateTableNumber(OrderType type, String tableNumber) {
        if (type == OrderType.MESA && (tableNumber == null || tableNumber.isBlank()))
            throw new IllegalArgumentException("Número da mesa é obrigatório para pedidos do tipo MESA.");
    }
}
