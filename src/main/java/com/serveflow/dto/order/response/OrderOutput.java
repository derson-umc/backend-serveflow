package com.serveflow.dto.order.response;

import com.serveflow.model.address.Address;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderOutput(
        UUID id,
        String customerName,
        AddressOutput address,
        String type,
        String status,
        LocalDateTime createdAt,
        String observation,
        BigDecimal totalValue,
        List<OrderItemOutput> items
) {
    public record AddressOutput(
            UUID id,
            String cep,
            String street,
            String city,
            String state,
            String number,
            String complement
    ) {
        public static AddressOutput from(Address a) {
            if (a == null) return null;
            return new AddressOutput(
                    a.getId(),
                    a.getCep() != null ? a.getCep().getValue() : null,
                    a.getStreet() != null ? a.getStreet().getValue() : null,
                    a.getCity() != null ? a.getCity().getValue() : null,
                    a.getState() != null ? a.getState().getValue().name() : null,
                    a.getNumber() != null ? a.getNumber().getValue() : null,
                    a.getComplement() != null ? a.getComplement().getValue() : null
            );
        }
    }

    public record OrderItemOutput(
            UUID id,
            UUID productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            String observation,
            BigDecimal total,
            List<ItemAdditionalOutput> additionals
    ) {}

    public record ItemAdditionalOutput(
            UUID id,
            String name,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal total
    ) {}
}
