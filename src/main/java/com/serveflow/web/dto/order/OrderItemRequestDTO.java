package com.serveflow.web.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemRequestDTO(

        @NotBlank(message = "Nome do produto e obrigatorio")
        String productName,

        @NotNull(message = "Quantidade e obrigatoria")
        @Min(value = 1, message = "Quantidade deve ser no minimo 1")
        Integer quantity,

        @NotNull(message = "Preco unitario e obrigatorio")
        @DecimalMin(value = "0.01", message = "Preco unitario deve ser maior que zero")
        BigDecimal unitPrice,

        String observation,

        @Valid
        List<ItemAdditionalRequestDTO> additionals
) {}
