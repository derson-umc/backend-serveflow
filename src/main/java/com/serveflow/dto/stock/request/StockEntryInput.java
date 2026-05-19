package com.serveflow.dto.stock.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockEntryInput(

        @NotNull(message = "Quantidade é obrigatória.")
        @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero.")
        BigDecimal quantity,

        String reason,

        String supplier,

        @DecimalMin(value = "0.0", message = "Custo unitário não pode ser negativo.")
        BigDecimal unitCost
) {}
