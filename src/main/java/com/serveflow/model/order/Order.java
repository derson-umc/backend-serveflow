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

    private final String createdBy;
    private OrderStatus status;
    private ComandaStatus comandaStatus;
    private String paymentMethod;
    private String cancelReason;
    private String canceledBy;
    private LocalDateTime canceledAt;
    private Long version;

    private Order(Builder builder) {
        this.id            = Objects.requireNonNull(builder.id,          "ID do pedido é obrigatório.");
        this.customerName  = Objects.requireNonNull(builder.customerName, "Nome do cliente é obrigatório.");
        this.type          = Objects.requireNonNull(builder.type,         "Tipo do pedido é obrigatório.");
        this.status        = Objects.requireNonNull(builder.status,       "Status do pedido é obrigatório.");
        this.createdAt     = Objects.requireNonNull(builder.createdAt,    "Data de criação é obrigatória.");
        this.address       = builder.address;
        this.createdBy     = builder.createdBy;
        this.comandaStatus = builder.comandaStatus != null ? builder.comandaStatus : ComandaStatus.ABERTA;
        this.observation   = builder.observation != null ? builder.observation.strip() : null;
        this.paymentMethod = builder.paymentMethod;
        this.tableNumber   = builder.tableNumber;
        this.cancelReason  = builder.cancelReason;
        this.canceledBy    = builder.canceledBy;
        this.canceledAt    = builder.canceledAt;
        this.items         = new ArrayList<>(Optional.ofNullable(builder.items).orElse(List.of()));
        this.version       = builder.version;
    }

    public static Order create(String customerName, Address address,
                               OrderType type, String observation, String tableNumber,
                               String createdBy) {
        validateCustomerName(customerName);
        validateDeliveryAddress(type, address);
        validateTableNumber(type, tableNumber);

        return Order.builder()
                .id(UUID.randomUUID())
                .customerName(customerName.strip())
                .address(type == OrderType.DELIVERY ? address : null)
                .type(type)
                .status(OrderStatus.PENDENTE)
                .comandaStatus(ComandaStatus.ABERTA)
                .createdAt(LocalDateTime.now())
                .observation(observation)
                .tableNumber(tableNumber)
                .createdBy(createdBy)
                .items(new ArrayList<>())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID id;
        private String customerName;
        private Address address;
        private OrderType type;
        private OrderStatus status;
        private ComandaStatus comandaStatus;
        private LocalDateTime createdAt;
        private String observation;
        private String paymentMethod;
        private String tableNumber;
        private String cancelReason;
        private String canceledBy;
        private LocalDateTime canceledAt;
        private String createdBy;
        private List<OrderItem> items;
        private Long version;

        private Builder() {}

        public Builder id(UUID val)                    { this.id            = val; return this; }
        public Builder customerName(String val)         { this.customerName  = val; return this; }
        public Builder address(Address val)             { this.address       = val; return this; }
        public Builder type(OrderType val)              { this.type          = val; return this; }
        public Builder status(OrderStatus val)          { this.status        = val; return this; }
        public Builder comandaStatus(ComandaStatus val) { this.comandaStatus = val; return this; }
        public Builder createdAt(LocalDateTime val)     { this.createdAt     = val; return this; }
        public Builder observation(String val)          { this.observation   = val; return this; }
        public Builder paymentMethod(String val)        { this.paymentMethod = val; return this; }
        public Builder tableNumber(String val)          { this.tableNumber   = val; return this; }
        public Builder cancelReason(String val)         { this.cancelReason  = val; return this; }
        public Builder canceledBy(String val)           { this.canceledBy    = val; return this; }
        public Builder canceledAt(LocalDateTime val)    { this.canceledAt    = val; return this; }
        public Builder createdBy(String val)            { this.createdBy     = val; return this; }
        public Builder items(List<OrderItem> val)       { this.items         = val; return this; }
        public Builder version(Long val)                { this.version       = val; return this; }

        public Order build() { return new Order(this); }
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

    public void cancelItem(UUID itemId, String reason) {
        if (comandaStatus.isClosed())
            throw new IllegalStateException("Comanda fechada não permite cancelamento de itens.");
        if (status == OrderStatus.CANCELADO)
            throw new IllegalArgumentException("Pedido cancelado não permite alterações.");
        if (status == OrderStatus.PENDENTE)
            throw new IllegalArgumentException("Pedidos pendentes devem ser editados pela edição completa.");
        findItemById(itemId).cancel(resolveCancelStatus(), reason);
    }

    public void appendItems(List<OrderItem> newItems) {
        Objects.requireNonNull(newItems, "Lista de itens é obrigatória.");
        if (newItems.isEmpty())
            throw new IllegalArgumentException("Informe ao menos um item para adicionar.");
        if (comandaStatus.isClosed())
            throw new IllegalStateException("Comanda fechada não aceita novos itens.");
        if (status == OrderStatus.CANCELADO)
            throw new IllegalStateException("Pedido cancelado não aceita novos itens.");
        if (status == OrderStatus.PENDENTE)
            throw new IllegalStateException("Pedido pendente deve ser editado pela edição completa.");
        if (status == OrderStatus.ENTREGUE)
            this.status = OrderStatus.ENVIADO;
        newItems.forEach(item -> {
            item.syncStatus(OrderItemStatus.ENVIADO);
            items.add(item);
        });
    }

    public void replaceItems(List<OrderItem> newItems) {
        Objects.requireNonNull(newItems, "Lista de itens é obrigatória.");
        if (newItems.isEmpty())
            throw new IllegalArgumentException("Pedido deve conter ao menos um item.");
        if (status != OrderStatus.PENDENTE)
            throw new IllegalStateException("Itens só podem ser substituídos em pedidos com status PENDENTE.");
        items.clear();
        items.addAll(newItems);
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

    public void requestPayment() {
        transitionTo(OrderStatus.AGUARDANDO_PAGAMENTO);
        this.comandaStatus = ComandaStatus.EM_FECHAMENTO;
    }

    public void complete() {
        transitionTo(OrderStatus.ENTREGUE);
    }

    public void closeComanda() {
        if (comandaStatus.isClosed())
            throw new IllegalStateException("Comanda já está fechada.");
        this.comandaStatus = ComandaStatus.FECHADA;
    }

    public void cancel(String reason, String canceledBy) {
        cancelAllActiveItems(reason);
        this.cancelReason  = reason;
        this.canceledBy    = canceledBy;
        this.canceledAt    = LocalDateTime.now();
        this.comandaStatus = ComandaStatus.FECHADA;
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

    public boolean isFinalized() { return status.isFinal() || comandaStatus.isClosed(); }
    public boolean isDelivery()  { return type == OrderType.DELIVERY; }
    public int getItemCount()    { return items.size(); }

    public UUID getId()                     { return id; }
    public String getCustomerName()         { return customerName; }
    public Address getAddress()             { return address; }
    public OrderType getType()              { return type; }
    public OrderStatus getStatus()          { return status; }
    public ComandaStatus getComandaStatus() { return comandaStatus; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public String getObservation()          { return observation; }
    public String getPaymentMethod()        { return paymentMethod; }
    public String getTableNumber()          { return tableNumber; }
    public String getCancelReason()         { return cancelReason; }
    public String getCanceledBy()           { return canceledBy; }
    public LocalDateTime getCanceledAt()    { return canceledAt; }
    public String getCreatedBy()            { return createdBy; }
    public List<OrderItem> getItems()       { return List.copyOf(items); }
    public Long getVersion()                { return version; }

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

    private OrderItem findItemById(UUID itemId) {
        return items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado no pedido."));
    }

    private OrderItemStatus resolveCancelStatus() {
        return status == OrderStatus.EM_PREPARO
                ? OrderItemStatus.CANCELADO_EM_PREPARO
                : OrderItemStatus.CANCELADO_ANTES_PREPARO;
    }

    private void cancelAllActiveItems(String reason) {
        OrderItemStatus cancelStatus = resolveCancelStatus();
        items.stream()
                .filter(item -> !item.getStatus().isCanceled())
                .forEach(item -> item.cancel(cancelStatus, reason));
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
