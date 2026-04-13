package com.serveflow.web.dto.order.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemAdditionalInput(

        @NotBlank(message = "Nome do adicional é obrigatório.")
        String name,

        @NotNull(message = "Quantidade do adicional é obrigatória.")
        @Min(value = 1, message = "Quantidade do adicional deve ser no mínimo 1.")
        Integer quantity,

        @NotNull(message = "Preço unitário do adicional é obrigatório.")
        @DecimalMin(value = "0.01", message = "Preço unitário do adicional deve ser maior que zero.")
        BigDecimal unitPrice
) {}
