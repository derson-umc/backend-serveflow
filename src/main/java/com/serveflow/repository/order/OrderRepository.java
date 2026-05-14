package com.serveflow.repository.order;

import com.serveflow.exception.order.OrderNotFound;
import com.serveflow.model.address.*;
import com.serveflow.model.address.Number;
import com.serveflow.model.order.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Repository
@Transactional(readOnly = true)
public class OrderRepository {

    private final SpringOrderRepository springRepository;

    public OrderRepository(SpringOrderRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Transactional
    public Order save(Order order) {
        boolean isNew = order.getVersion() == null;
        OrderEntity entity;

        if (isNew) {
            entity = toEntity(order);
        } else {
            entity = springRepository.findById(order.getId())
                    .orElseThrow(() -> new OrderNotFound(order.getId()));
            updateEntity(entity, order);
        }

        return toDomain(springRepository.save(entity));
    }

    public Order findById(UUID id) {
        return springRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new OrderNotFound(id));
    }

    public List<Order> findAll() {
        return springRepository.findAll().stream().map(this::toDomain).toList();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return springRepository.findByStatus(status).stream().map(this::toDomain).toList();
    }

    private Order toDomain(OrderEntity e) {
        return new Order(
                e.getIdOrder(),
                e.getCustomerName(),
                e.getAddress() != null ? toAddressDomain(e.getAddress()) : null,
                e.getType(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getObservation(),
                e.getItems().stream().map(this::toItemDomain).toList(),
                e.getVersion()
        );
    }

    private OrderItem toItemDomain(OrderItemEntity e) {
        return new OrderItem(
                e.getIdOrderItem(),
                e.getProductId(),
                e.getProductName(),
                e.getQuantity(),
                e.getUnitPrice(),
                e.getObservation(),
                e.getAdditionals().stream().map(this::toAdditionalDomain).toList()
        );
    }

    private ItemAdditional toAdditionalDomain(ItemAdditionalEntity e) {
        return new ItemAdditional(e.getIdItemAdditional(), e.getName(), e.getQuantity(), e.getUnitPrice());
    }

    private Address toAddressDomain(AddressEntity e) {
        return Address.withId(
                e.getIdAddress(),
                e.getCep() != null ? new Cep(e.getCep()) : null,
                new Street(e.getStreet()),
                new City(e.getCity()),
                new State(e.getState()),
                new Number(e.getNumber()),
                e.getComplement() != null ? new Complement(e.getComplement()) : null
        );
    }

    private OrderEntity toEntity(Order order) {
        return updateEntity(new OrderEntity(), order);
    }

    private OrderEntity updateEntity(OrderEntity entity, Order order) {
        entity.setIdOrder(order.getId());
        entity.setVersion(order.getVersion());
        entity.setCustomerName(order.getCustomerName());
        entity.setType(order.getType());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setObservation(order.getObservation());

        if (order.getAddress() != null) {
            entity.setAddress(toAddressEntity(order.getAddress()));
        }

        List<OrderItemEntity> updatedItems = order.getItems().stream()
                .map(item -> syncItemEntity(
                        findOrNew(entity.getItems(), item.getId(),
                                OrderItemEntity::getIdOrderItem, OrderItemEntity::new),
                        item, entity))
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(updatedItems);

        return entity;
    }

    private OrderItemEntity syncItemEntity(OrderItemEntity entity, OrderItem item, OrderEntity order) {
        entity.setIdOrderItem(item.getId());
        entity.setOrder(order);
        entity.setProductId(item.getProductId());
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setObservation(item.getObservation());

        List<ItemAdditionalEntity> updatedAdditionals = item.getAdditionals().stream()
                .map(add -> syncAdditionalEntity(
                        findOrNew(entity.getAdditionals(), add.getId(),
                                ItemAdditionalEntity::getIdItemAdditional, ItemAdditionalEntity::new),
                        add, entity))
                .toList();

        entity.getAdditionals().clear();
        entity.getAdditionals().addAll(updatedAdditionals);

        return entity;
    }

    private ItemAdditionalEntity syncAdditionalEntity(ItemAdditionalEntity entity,
                                                       ItemAdditional add,
                                                       OrderItemEntity item) {
        entity.setIdItemAdditional(add.getId());
        entity.setOrderItem(item);
        entity.setName(add.getName());
        entity.setQuantity(add.getQuantity());
        entity.setUnitPrice(add.getUnitPrice());
        return entity;
    }

    private AddressEntity toAddressEntity(Address a) {
        AddressEntity entity = new AddressEntity();
        entity.setIdAddress(a.getId());
        entity.setCep(a.getCep() != null ? a.getCep().getValue() : null);
        entity.setStreet(a.getStreet() != null ? a.getStreet().getValue() : null);
        entity.setCity(a.getCity() != null ? a.getCity().getValue() : null);
        entity.setState(a.getState() != null ? a.getState().getValue().name() : null);
        entity.setNumber(a.getNumber() != null ? a.getNumber().getValue() : null);
        entity.setComplement(a.getComplement() != null ? a.getComplement().getValue() : null);
        return entity;
    }

    private <E, ID> E findOrNew(List<E> list, ID id, Function<E, ID> idGetter, Supplier<E> factory) {
        return list.stream()
                .filter(e -> id != null && id.equals(idGetter.apply(e)))
                .findFirst()
                .orElseGet(factory);
    }
}
