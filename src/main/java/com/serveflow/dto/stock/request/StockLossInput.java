package com.serveflow.dto.stock.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockLossInput(

        @NotNull(message = "Quantidade é obrigatória.")
        @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero.")
        BigDecimal quantity,

        @NotBlank(message = "Motivo da perda é obrigatório.")
        String reason
) {}
