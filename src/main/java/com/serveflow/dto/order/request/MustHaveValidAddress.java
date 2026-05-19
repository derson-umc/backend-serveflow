package com.serveflow.dto.order.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnsureValidDelivery.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MustHaveValidAddress {
    String message() default "Endereço é obrigatório para pedidos de delivery.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
