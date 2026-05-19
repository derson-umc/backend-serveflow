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
    private final List<OrderItem> items;

    private OrderStatus status;
    private String paymentMethod;
    private Long version;

    public static Order create(String customerName, Address address,
                               OrderType type, String observation) {
        validateCustomerName(customerName);
        validateDeliveryAddress(type, address);

        return new Order(
                UUID.randomUUID(),
                customerName.strip(),
                type == OrderType.DELIVERY ? address : null,
                type,
                OrderStatus.CREATED,
                LocalDateTime.now(),
                observation,
                null,
                new ArrayList<>(),
                null
        );
    }

    public Order(UUID id, String customerName, Address address, OrderType type,
                 OrderStatus status, LocalDateTime createdAt, String observation,
                 String paymentMethod, List<OrderItem> items, Long version) {

        this.id = Objects.requireNonNull(id, "ID do pedido é obrigatório.");
        this.customerName = Objects.requireNonNull(customerName, "Nome do cliente é obrigatório.");
        this.address = address;
        this.type = Objects.requireNonNull(type, "Tipo do pedido é obrigatório.");
        this.status = Objects.requireNonNull(status, "Status do pedido é obrigatório.");
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criação é obrigatória.");
        this.observation = observation != null ? observation.strip() : null;
        this.paymentMethod = paymentMethod;
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
        transitionTo(OrderStatus.CONFIRMED);
    }

    public void startPreparation() { transitionTo(OrderStatus.IN_PREPARATION); }
    public void markReady()         { transitionTo(OrderStatus.READY); }

    public void sendForDelivery() {
        if (type != OrderType.DELIVERY)
            throw new IllegalStateException("Apenas pedidos delivery podem ser enviados para entrega.");
        transitionTo(OrderStatus.OUT_FOR_DELIVERY);
    }

    public void complete() { transitionTo(OrderStatus.DELIVERED); }
    public void cancel()   { transitionTo(OrderStatus.CANCELLED); }

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

    public UUID getId()              { return id; }
    public String getCustomerName()  { return customerName; }
    public Address getAddress()      { return address; }
    public OrderType getType()       { return type; }
    public OrderStatus getStatus()   { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getObservation()   { return observation; }
    public String getPaymentMethod() { return paymentMethod; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public Long getVersion()         { return version; }

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
}
