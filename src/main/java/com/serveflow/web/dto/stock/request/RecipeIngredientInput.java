package com.serveflow.web.dto.stock.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record RecipeIngredientInput(
    @NotNull(message = "ID do insumo e obrigatorio.")
    UUID stockItemId,

    @NotBlank(message = "Nome do insumo e obrigatorio.")
    @Size(max = 100, message = "Nome deve ter no maximo 100 caracteres.")
    String stockItemName,

    @NotNull(message = "Quantidade por unidade e obrigatoria.")
    @DecimalMin(value = "0.0001", message = "Quantidade por unidade deve ser maior que zero.")
    BigDecimal quantityPerUnit,

    @NotBlank(message = "Unidade de medida e obrigatoria.")
    @Size(max = 20, message = "Unidade deve ter no maximo 20 caracteres.")
    String unit
) {}
