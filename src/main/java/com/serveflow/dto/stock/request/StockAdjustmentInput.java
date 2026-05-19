package com.serveflow.dto.stock.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockAdjustmentInput(

        @NotNull(message = "Quantidade nova é obrigatória.")
        @DecimalMin(value = "0.0", message = "Quantidade não pode ser negativa.")
        BigDecimal newQuantity,

        @NotBlank(message = "Motivo do ajuste é obrigatório.")
        String reason
) {}
