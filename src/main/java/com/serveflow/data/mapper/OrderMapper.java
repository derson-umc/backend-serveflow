package com.serveflow.data.mapper;

import com.serveflow.data.entity.*;
import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.model.order.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public Order toDomain(OrderEntity entity) {
        Address address = entity.getAddress() != null
                ? toAddressDomain(entity.getAddress())
                : null;

        List<OrderItem> items = entity.getItems().stream()
                .map(this::toItemDomain)
                .toList();

        return new Order(
                entity.getIdOrder(),
                entity.getCustomerName(),
                address,
                OrderType.valueOf(entity.getType().name()),
                OrderStatus.valueOf(entity.getStatus().name()),
                entity.getCreatedAt(),
                entity.getObservation(),
                items
        );
    }

    public OrderEntity toEntity(Order order) {
        var entity = new OrderEntity();
        entity.setIdOrder(order.getId());
        entity.setCustomerName(order.getCustomerName());
        entity.setType(OrderEntity.OrderType.valueOf(order.getType().name()));
        entity.setStatus(OrderEntity.OrderStatus.valueOf(order.getStatus().name()));
        entity.setCreatedAt(order.getCreatedAt());
        entity.setObservation(order.getObservation());

        if (order.getAddress() != null) {
            entity.setAddress(toAddressEntity(order.getAddress()));
        }

        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> toItemEntity(item, entity))
                .toList();
        entity.setItems(itemEntities);

        return entity;
    }

    private Address toAddressDomain(AddressEntity entity) {
        return new Address(
                entity.getIdAddress(),
                entity.getCep(),
                entity.getStreet(),
                entity.getCity(),
                entity.getState(),
                entity.getNumber(),
                entity.getComplement()
        );
    }

    private AddressEntity toAddressEntity(Address address) {
        var entity = new AddressEntity();
        entity.setIdAddress(address.getId());
        entity.setCep(address.getCep());
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setState(address.getState());
        entity.setNumber(address.getNumber());
        entity.setComplement(address.getComplement());
        return entity;
    }

    private OrderItem toItemDomain(OrderItemEntity entity) {
        List<ItemAdditional> additionals = entity.getAdditionals().stream()
                .map(this::toAdditionalDomain)
                .toList();

        return new OrderItem(
                entity.getIdOrderItem(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getObservation(),
                additionals
        );
    }

    private OrderItemEntity toItemEntity(OrderItem item, OrderEntity order) {
        var entity = new OrderItemEntity();
        entity.setIdOrderItem(item.getId());
        entity.setOrder(order);
        entity.setProductId(item.getProductId());
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setObservation(item.getObservation());

        List<ItemAdditionalEntity> additionalEntities = item.getAdditionals().stream()
                .map(add -> toAdditionalEntity(add, entity))
                .toList();
        entity.setAdditionals(additionalEntities);

        return entity;
    }

    private ItemAdditional toAdditionalDomain(ItemAdditionalEntity entity) {
        return new ItemAdditional(
                entity.getIdItemAdditional(),
                entity.getName(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );
    }

    private ItemAdditionalEntity toAdditionalEntity(ItemAdditional additional, OrderItemEntity orderItem) {
        var entity = new ItemAdditionalEntity();
        entity.setIdItemAdditional(additional.getId());
        entity.setOrderItem(orderItem);
        entity.setName(additional.getName());
        entity.setQuantity(additional.getQuantity());
        entity.setUnitPrice(additional.getUnitPrice());
        return entity;
    }
}
