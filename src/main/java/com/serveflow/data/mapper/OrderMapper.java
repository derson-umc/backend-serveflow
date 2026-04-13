package com.serveflow.data.mapper;

import com.serveflow.data.entity.address.AddressEntity;
import com.serveflow.data.entity.order.ItemAdditionalEntity;
import com.serveflow.data.entity.order.OrderEntity;
import com.serveflow.data.entity.order.OrderItemEntity;
import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.model.order.*;
import org.springframework.stereotype.Component;
import java.util.function.Function;
import java.util.function.Supplier;

import java.util.List;

@Component
public class OrderMapper {

    public Order toDomain(OrderEntity e) {
        return new Order(
                e.getIdOrder(),
                e.getCustomerName(),
                e.getAddress() != null ? toDomain(e.getAddress()) : null,
                e.getType(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getObservation(),
                e.getItems().stream().map(this::toDomain).toList(),
                e.getVersion()
        );
    }

    private OrderItem toDomain(OrderItemEntity e) {
        return new OrderItem(
                e.getIdOrderItem(),
                e.getProductId(),
                e.getProductName(),
                e.getQuantity(),
                e.getUnitPrice(),
                e.getObservation(),
                e.getAdditionals().stream().map(this::toDomain).toList()
        );
    }

    private ItemAdditional toDomain(ItemAdditionalEntity e) {
        return new ItemAdditional(e.getIdItemAdditional(), e.getName(), e.getQuantity(), e.getUnitPrice());
    }

    private Address toDomain(AddressEntity e) {
        return new Address(e.getIdAddress(), e.getCep(), e.getStreet(), e.getCity(), e.getState(), e.getNumber(), e.getComplement());
    }

    public OrderEntity toEntity(Order order) {
        return updateEntity(new OrderEntity(), order);
    }

    public OrderEntity updateEntity(OrderEntity entity, Order order) {
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

        var updatedItems = order.getItems().stream()
                .map(item -> syncItemEntity(
                        findOrNew(entity.getItems(), item.getId(), OrderItemEntity::getIdOrderItem, OrderItemEntity::new),
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

        var updatedAdditionals = item.getAdditionals().stream()
                .map(add -> syncAdditionalEntity(
                        findOrNew(entity.getAdditionals(), add.getId(), ItemAdditionalEntity::getIdItemAdditional, ItemAdditionalEntity::new),
                        add, entity))
                .toList();

        entity.getAdditionals().clear();
        entity.getAdditionals().addAll(updatedAdditionals);

        return entity;
    }

    private ItemAdditionalEntity syncAdditionalEntity(ItemAdditionalEntity entity, ItemAdditional add, OrderItemEntity item) {
        entity.setIdItemAdditional(add.getId());
        entity.setOrderItem(item);
        entity.setName(add.getName());
        entity.setQuantity(add.getQuantity());
        entity.setUnitPrice(add.getUnitPrice());
        return entity;
    }

    private AddressEntity toAddressEntity(Address a) {
        var entity = new AddressEntity();
        entity.setIdAddress(a.getId());
        entity.setCep(a.getCep());
        entity.setStreet(a.getStreet());
        entity.setCity(a.getCity());
        entity.setState(a.getState());
        entity.setNumber(a.getNumber());
        entity.setComplement(a.getComplement());
        return entity;
    }

    private <E, ID> E findOrNew(List<E> list, ID id, Function<E, ID> idGetter, Supplier<E> factory) {
        return list.stream()
                .filter(e -> id != null && id.equals(idGetter.apply(e)))
                .findFirst()
                .orElseGet(factory);
    }
}