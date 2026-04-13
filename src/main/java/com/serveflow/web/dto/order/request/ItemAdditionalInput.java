package com.serveflow.web.dto.order.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemAdditionalInput(

        @NotBlank(message = "Nome do adicional e obrigatorio.")
        String name,

        @NotNull(message = "Quantidade do adicional e obrigatoria.")
        @Min(value = 1, message = "Quantidade do adicional deve ser no minimo 1.")
        Integer quantity,

        @NotNull(message = "Preco unitario do adicional e obrigatorio.")
        @DecimalMin(value = "0.01", message = "Preco unitario do adicional deve ser maior que zero.")
        BigDecimal unitPrice
) {}
