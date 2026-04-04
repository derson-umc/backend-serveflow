package com.serveflow.web.mapper;

import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.model.order.*;
import com.serveflow.web.dto.address.AddressRequestDTO;
import com.serveflow.web.dto.order.*;
import org.springframework.stereotype.Component;
import com.serveflow.web.dto.order.ItemAdditionalRequestDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderWebMapper {

    public Order toDomain(OrderRequestDTO request) {
        return toDomain(request, mapAddress(request.address()));
    }

    public Order toDomain(OrderRequestDTO request, Address resolvedAddress) {
        OrderType orderType = parseOrderType(request.type());

        Order order = Order.create(request.customerName(), resolvedAddress, orderType,
                request.observation());

        toItemsDomain(request.items()).forEach(order::addItem);

        return order;
    }

    public List<OrderItem> toItemsDomain(List<OrderItemRequestDTO> items) {
        return items.stream()
                .map(this::toItemDomain)
                .toList();
    }

    public OrderResponseDTO toResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerName(),
                mapAddressResponse(order.getAddress()),
                order.getType().name(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getObservation(),
                order.getTotal(),
                mapItemsResponse(order.getItems())
        );
    }

    public List<OrderResponseDTO> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderType parseOrderType(String type) {
        return OrderType.valueOf(type.toUpperCase());
    }

    public Address toAddressDomain(AddressRequestDTO dto) {
        if (dto == null) return null;
        boolean hasManualFields = dto.street() != null && !dto.street().isBlank()
                && dto.city() != null && !dto.city().isBlank()
                && dto.state() != null && !dto.state().isBlank()
                && dto.number() != null && !dto.number().isBlank();
        if (!hasManualFields) return null;
        return new Address(
                dto.cep(), dto.street(), dto.city(),
                dto.state(), dto.number(), dto.complement()
        );
    }

    private Address mapAddress(AddressRequestDTO dto) {
        return toAddressDomain(dto);
    }

    private OrderResponseDTO.AddressResponseDTO mapAddressResponse(Address address) {
        return address != null
                ? new OrderResponseDTO.AddressResponseDTO(
                address.getId(), address.getCep(), address.getStreet(),
                address.getCity(), address.getState(), address.getNumber(),
                address.getComplement())
                : null;
    }

    private OrderItem toItemDomain(OrderItemRequestDTO dto) {
        List<ItemAdditional> additionals = Optional.ofNullable(dto.additionals())
                .orElse(List.of())
                .stream()
                .map(this::toAdditionalDomain)
                .toList();

        return new OrderItem(dto.productName(), dto.quantity(), dto.unitPrice(),
                dto.observation(), additionals);
    }

    private ItemAdditional toAdditionalDomain(ItemAdditionalRequestDTO additional) {
        return new ItemAdditional(
                additional.name(),
                additional.quantity(),
                additional.unitPrice()
        );
    }

    private List<OrderResponseDTO.OrderItemResponseDTO> mapItemsResponse(List<OrderItem> items) {
        return items.stream()
                .map(this::toItemResponse)
                .toList();
    }

    private OrderResponseDTO.OrderItemResponseDTO toItemResponse(OrderItem item) {
        List<OrderResponseDTO.ItemAdditionalResponseDTO> additionals = item.getAdditionals().stream()
                .map(this::toAdditionalResponse)
                .toList();

        return new OrderResponseDTO.OrderItemResponseDTO(
                item.getId(), item.getProductName(), item.getQuantity(),
                item.getUnitPrice(), item.getObservation(), item.getTotal(), additionals
        );
    }

    private OrderResponseDTO.ItemAdditionalResponseDTO toAdditionalResponse(ItemAdditional additional) {
        return new OrderResponseDTO.ItemAdditionalResponseDTO(
                additional.getId(), additional.getName(), additional.getQuantity(),
                additional.getUnitPrice(), additional.getTotal()
        );
    }
}