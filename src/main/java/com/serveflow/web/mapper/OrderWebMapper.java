package com.serveflow.web.mapper;

import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.model.order.*;
import com.serveflow.web.dto.order.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderWebMapper {

    public Order toDomain(OrderRequestDTO request) {
        OrderType type = OrderType.valueOf(request.type().toUpperCase());
        Address address = request.address() != null ? toAddressDomain(request.address()) : null;
        return Order.create(request.customerName(), address, type, request.observation());
    }

    public List<OrderItem> toItemsDomain(List<OrderItemRequestDTO> items) {
        return items.stream().map(this::toItemDomain).toList();
    }

    public OrderResponseDTO toResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerName(),
                toAddressResponse(order.getAddress()),
                order.getType().name(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getObservation(),
                order.getTotal(),
                toItemsResponse(order.getItems())
        );
    }

    public List<OrderResponseDTO> toResponseList(List<Order> orders) {
        return orders.stream().map(this::toResponse).toList();
    }

    // --- Private helpers ---

    private OrderItem toItemDomain(OrderItemRequestDTO dto) {
        List<ItemAdditional> additionals = Optional.ofNullable(dto.additionals())
                .orElse(List.of())
                .stream()
                .map(a -> new ItemAdditional(a.name(), a.quantity(), a.unitPrice()))
                .toList();

        return new OrderItem(
                dto.productId(), dto.productName(), dto.quantity(),
                dto.unitPrice(), dto.observation(), additionals
        );
    }

    private Address toAddressDomain(AddressRequestDTO dto) {
        return new Address(
                dto.cep(), dto.street(), dto.city(),
                dto.state(), dto.number(), dto.complement()
        );
    }

    private OrderResponseDTO.AddressResponseDTO toAddressResponse(Address address) {
        if (address == null) return null;
        return new OrderResponseDTO.AddressResponseDTO(
                address.getId(), address.getCep(), address.getStreet(),
                address.getCity(), address.getState(), address.getNumber(),
                address.getComplement()
        );
    }

    private List<OrderResponseDTO.OrderItemResponseDTO> toItemsResponse(List<OrderItem> items) {
        return items.stream().map(this::toItemResponse).toList();
    }

    private OrderResponseDTO.OrderItemResponseDTO toItemResponse(OrderItem item) {
        List<OrderResponseDTO.ItemAdditionalResponseDTO> additionals = item.getAdditionals().stream()
                .map(a -> new OrderResponseDTO.ItemAdditionalResponseDTO(
                        a.getId(), a.getName(), a.getQuantity(), a.getUnitPrice(), a.getTotal()
                ))
                .toList();

        return new OrderResponseDTO.OrderItemResponseDTO(
                item.getId(), item.getProductId(), item.getProductName(),
                item.getQuantity(), item.getUnitPrice(), item.getObservation(),
                item.getTotal(), additionals
        );
    }
}
