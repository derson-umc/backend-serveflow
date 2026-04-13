package com.serveflow.web.dto.stock.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record StockItemInput(
        @NotNull(message = "ID do insumo é obrigatório.")
        UUID stockItemId,

        @NotBlank(message = "Nome do insumo é obrigatório.")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
        String stockItemName,

        @NotNull(message = "Quantidade por unidade é obrigatória.")
        @DecimalMin(value = "0.0001", message = "Quantidade por unidade deve ser maior que zero.")
        BigDecimal quantityPerUnit,

        @NotBlank(message = "Unidade de medida é obrigatória.")
        @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres.")
        String unit
) {

}

