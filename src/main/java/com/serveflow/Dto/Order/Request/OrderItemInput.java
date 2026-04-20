package com.serveflow.Dto.Order.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderItemInput(

        @NotNull(message = "ID do produto é obrigatório.")
        UUID productId,

        @NotBlank(message = "Nome do produto é obrigatório.")
        String productName,

        @NotNull(message = "Quantidade é obrigatória.")
        @Min(value = 1, message = "Quantidade deve ser no mínimo 1.")
        Integer quantity,

        @NotNull(message = "Preço unitário é obrigatório.")
        @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero.")
        BigDecimal unitPrice,

        String observation,

        @Valid
        List<ItemAdditionalInput> additionals
) {}
