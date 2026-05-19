package com.serveflow.dto.stock.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StockItemInput(

        @NotBlank(message = "Nome do insumo é obrigatório.")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "Unidade de medida é obrigatória.")
        @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres.")
        String unit,

        @NotNull(message = "Quantidade atual é obrigatória.")
        @DecimalMin(value = "0.0", message = "Quantidade atual não pode ser negativa.")
        BigDecimal currentQuantity,

        @NotNull(message = "Quantidade mínima é obrigatória.")
        @DecimalMin(value = "0.0", message = "Quantidade mínima não pode ser negativa.")
        BigDecimal minimumQuantity,

        String category,

        String supplier,

        @DecimalMin(value = "0.0", message = "Custo médio não pode ser negativo.")
        BigDecimal averageCost
) {}
