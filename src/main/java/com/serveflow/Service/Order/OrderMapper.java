package com.serveflow.Service.Order;

import com.serveflow.Dto.Order.Response.OrderOutput;
import com.serveflow.Model.Address.Address;
import com.serveflow.Model.Order.Order;
import com.serveflow.Model.Order.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderOutput toOutput(Order order) {
        return new OrderOutput(
                order.getId(),
                order.getCustomerName(),
                order.getTableNumber(),
                order.getWaiterId(),
                toAddressOutput(order.getAddress()),
                order.getType().name(),
                order.getStatus().name(),
                order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
                order.getCreatedAt(),
                order.getObservation(),
                order.getTotal(),
                order.getItems().stream().map(this::toItemOutput).toList()
        );
    }

    private OrderOutput.AddressOutput toAddressOutput(Address a) {
        if (a == null) return null;
        return new OrderOutput.AddressOutput(
                a.getId(),
                a.getCep() != null ? a.getCep().getValue() : null,
                a.getStreet() != null ? a.getStreet().getValue() : null,
                a.getCity() != null ? a.getCity().getValue() : null,
                a.getState() != null ? a.getState().getValue().name() : null,
                a.getNumber() != null ? a.getNumber().getValue() : null,
                a.getComplement() != null ? a.getComplement().getValue() : null
        );
    }

    private OrderOutput.OrderItemOutput toItemOutput(OrderItem item) {
        return new OrderOutput.OrderItemOutput(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getObservation(),
                item.getTotal(),
                item.getAdditionals().stream().map(a ->
                        new OrderOutput.ItemAdditionalOutput(
                                a.getId(), a.getName(), a.getQuantity(), a.getUnitPrice(), a.getTotal())
                ).toList()
        );
    }
}
