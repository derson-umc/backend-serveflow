package com.serveflow.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DeliveryAddressValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDeliveryAddress {
    String message() default "Endereço e obrigatório para pedidos de delivery.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
