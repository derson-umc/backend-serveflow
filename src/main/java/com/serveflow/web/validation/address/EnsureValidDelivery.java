package com.serveflow.web.validation.address;

import com.serveflow.web.dto.order.request.CreateOrderInput;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnsureValidDelivery implements ConstraintValidator<MustHaveValidAddress, CreateOrderInput> {

    @Override
    public boolean isValid(CreateOrderInput dto, ConstraintValidatorContext context) {
        if (dto == null || dto.type() == null) return true;

        if ("DELIVERY".equalsIgnoreCase(dto.type()) && dto.address() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Endereço e obrigatório para pedidos de delivery.")
                    .addPropertyNode("address")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
