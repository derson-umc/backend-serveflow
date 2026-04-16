package com.serveflow.web.mapper;

import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.model.address.Cep;
import com.serveflow.domain.model.address.City;
import com.serveflow.domain.model.address.Complement;
import com.serveflow.domain.model.address.Number;
import com.serveflow.domain.model.address.State;
import com.serveflow.domain.model.address.Street;
import com.serveflow.domain.model.order.*;
import com.serveflow.web.dto.address.request.AddressInput;
import com.serveflow.web.dto.order.request.OrderInput;
import com.serveflow.web.dto.order.request.ItemAdditionalInput;
import com.serveflow.web.dto.order.request.OrderItemInput;
import com.serveflow.web.dto.order.response.OrderOutput;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderWebMapper {

    public Order toDomain(OrderInput request, Address resolvedAddress) {
        OrderType orderType = OrderType.valueOf(request.type().toUpperCase());

        Order order = Order.create(request.customerName(), resolvedAddress, orderType,
                request.observation());

        toItemsDomain(request.items()).forEach(order::addItem);

        return order;
    }

    public Address toAddressDomain(AddressInput dto) {
        if (dto == null) return null;
        boolean hasManualFields = dto.street() != null && !dto.street().isBlank()
                && dto.city() != null && !dto.city().isBlank()
                && dto.state() != null && !dto.state().isBlank()
                && dto.number() != null && !dto.number().isBlank();
        if (!hasManualFields) return null;
        return Address.create(
                dto.cep() != null && !dto.cep().isBlank() ? new Cep(dto.cep()) : null,
                new Street(dto.street()),
                new City(dto.city()),
                new State(dto.state()),
                new Number(dto.number()),
                dto.complement() != null && !dto.complement().isBlank() ? new Complement(dto.complement()) : null
        );
    }

    public OrderOutput toResponse(Order order) {
        return new OrderOutput(
                order.getId(),
                order.getCustomerName(),
                toAddressOutput(order.getAddress()),
                order.getType().name(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getObservation(),
                order.getTotal(),
                toItemsOutput(order.getItems())
        );
    }

    public List<OrderOutput> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .toList();
    }

    private List<OrderItem> toItemsDomain(List<OrderItemInput> items) {
        return items.stream()
                .map(this::toItemDomain)
                .toList();
    }

    private OrderItem toItemDomain(OrderItemInput dto) {
        List<ItemAdditional> additionals = Optional.ofNullable(dto.additionals())
                .orElse(List.of())
                .stream()
                .map(this::toAdditionalDomain)
                .toList();

        return new OrderItem(dto.productId(), dto.productName(), dto.quantity(), dto.unitPrice(),
                dto.observation(), additionals);
    }

    private ItemAdditional toAdditionalDomain(ItemAdditionalInput dto) {
        return new ItemAdditional(dto.name(), dto.quantity(), dto.unitPrice());
    }

    private OrderOutput.AddressOutput toAddressOutput(Address address) {
        if (address == null) return null;
        return new OrderOutput.AddressOutput(
                address.getId(),
                address.getCep() != null ? address.getCep().getValue() : null,
                address.getStreet() != null ? address.getStreet().getValue() : null,
                address.getCity() != null ? address.getCity().getValue() : null,
                address.getState() != null ? address.getState().getValue().name() : null,
                address.getNumber() != null ? address.getNumber().getValue() : null,
                address.getComplement() != null ? address.getComplement().getValue() : null
        );
    }

    private List<OrderOutput.OrderItemOutput> toItemsOutput(List<OrderItem> items) {
        return items.stream()
                .map(this::toItemOutput)
                .toList();
    }

    private OrderOutput.OrderItemOutput toItemOutput(OrderItem item) {
        List<OrderOutput.ItemAdditionalOutput> additionals = item.getAdditionals().stream()
                .map(this::toAdditionalOutput)
                .toList();

        return new OrderOutput.OrderItemOutput(
                item.getId(), item.getProductId(), item.getProductName(), item.getQuantity(),
                item.getUnitPrice(), item.getObservation(), item.getTotal(), additionals);
    }

    private OrderOutput.ItemAdditionalOutput toAdditionalOutput(ItemAdditional additional) {
        return new OrderOutput.ItemAdditionalOutput(
                additional.getId(), additional.getName(), additional.getQuantity(),
                additional.getUnitPrice(), additional.getTotal());
    }
}
