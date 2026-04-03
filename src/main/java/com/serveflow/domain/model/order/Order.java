package com.serveflow.domain.model.order;

import com.serveflow.domain.model.address.Address;

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
                new ArrayList<>()
        );
    }

    public Order(UUID id, String customerName, Address address, OrderType type,
                 OrderStatus status, LocalDateTime createdAt, String observation,
                 List<OrderItem> items) {

        this.id = Objects.requireNonNull(id, "ID do pedido e obrigatorio.");
        this.customerName = Objects.requireNonNull(customerName, "Nome do cliente e obrigatorio.");
        this.address = address;
        this.type = Objects.requireNonNull(type, "Tipo do pedido e obrigatorio.");
        this.status = Objects.requireNonNull(status, "Status do pedido e obrigatorio.");
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criacao e obrigatoria.");
        this.observation = observation != null ? observation.strip() : null;
        this.items = new ArrayList<>(Optional.ofNullable(items).orElse(List.of()));
    }

    // --- Item management ---

    public void addItem(OrderItem item) {
        ensureModifiable();
        Objects.requireNonNull(item, "Item nao pode ser nulo.");
        items.add(item);
    }

    public void removeItem(OrderItem item) {
        ensureModifiable();
        if (items.size() <= 1)
            throw new IllegalStateException("Pedido deve conter ao menos um item.");
        if (!items.remove(item))
            throw new IllegalArgumentException("Item nao encontrado no pedido.");
    }

    // --- Status transitions ---

    public void startPreparation() {
        ensureHasItems();
        transitionTo(OrderStatus.IN_PREPARATION);
    }

    public void markReady() {
        transitionTo(OrderStatus.READY);
    }

    public void sendForDelivery() {
        if (type != OrderType.DELIVERY)
            throw new IllegalStateException("Apenas pedidos delivery podem ser enviados para entrega.");
        transitionTo(OrderStatus.OUT_FOR_DELIVERY);
    }

    public void complete() {
        if (type == OrderType.DELIVERY) {
            transitionTo(OrderStatus.DELIVERED);
        } else {
            transitionTo(OrderStatus.DELIVERED);
        }
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    // --- Queries ---

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isFinalized() {
        return status.isFinal();
    }

    public boolean isDelivery() {
        return type == OrderType.DELIVERY;
    }

    public int getItemCount() {
        return items.size();
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public String getCustomerName() { return customerName; }
    public Address getAddress() { return address; }
    public OrderType getType() { return type; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getObservation() { return observation; }
    public List<OrderItem> getItems() { return List.copyOf(items); }

    // --- Equality ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    // --- Internal ---

    private void transitionTo(OrderStatus target) {
        if (!status.canTransitionTo(target))
            throw new IllegalStateException(
                    "Transicao de status invalida: " + status.getDescription()
                            + " -> " + target.getDescription() + ".");
        this.status = target;
    }

    private void ensureModifiable() {
        if (status.isFinal())
            throw new IllegalStateException("Pedido finalizado nao pode ser alterado.");
    }

    private void ensureHasItems() {
        if (items.isEmpty())
            throw new IllegalStateException("Pedido deve conter ao menos um item para iniciar preparacao.");
    }

    private static void validateCustomerName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome do cliente e obrigatorio.");
    }

    private static void validateDeliveryAddress(OrderType type, Address address) {
        if (type == OrderType.DELIVERY && address == null)
            throw new IllegalArgumentException("Endereco e obrigatorio para pedidos delivery.");
    }
}
