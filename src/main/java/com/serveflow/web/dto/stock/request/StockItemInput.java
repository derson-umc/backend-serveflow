package com.serveflow.web.dto.stock.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Valid
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
        BigDecimal minimumQuantity
) {}
