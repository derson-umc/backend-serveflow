package com.serveflow.dto.order.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnsureValidDelivery implements ConstraintValidator<MustHaveValidAddress, OrderInput> {

    @Override
    public boolean isValid(OrderInput dto, ConstraintValidatorContext context) {
        if (dto == null || dto.type() == null) return true;

        if ("DELIVERY".equalsIgnoreCase(dto.type()) && dto.address() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Endereço é obrigatório para pedidos de delivery.")
                    .addPropertyNode("address")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
