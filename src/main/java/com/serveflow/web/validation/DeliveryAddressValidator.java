package com.serveflow.web.validation;

import com.serveflow.web.dto.order.OrderRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DeliveryAddressValidator implements ConstraintValidator<ValidDeliveryAddress, OrderRequestDTO> {

    @Override
    public boolean isValid(OrderRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.type() == null) return true;

        if ("DELIVERY".equalsIgnoreCase(dto.type()) && dto.address() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Endereco e obrigatorio para pedidos delivery")
                    .addPropertyNode("address")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
