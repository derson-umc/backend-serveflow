package com.serveflow.web.dto.stock;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateStockItemRequestDTO(
    @NotBlank(message = "Nome do insumo e obrigatorio.")
    @Size(max = 100, message = "Nome deve ter no maximo 100 caracteres.")
    String name,

    @NotBlank(message = "Unidade de medida e obrigatoria.")
    @Size(max = 20, message = "Unidade deve ter no maximo 20 caracteres.")
    String unit,

    @NotNull(message = "Quantidade atual e obrigatoria.")
    @DecimalMin(value = "0.0", message = "Quantidade atual nao pode ser negativa.")
    BigDecimal currentQuantity,

    @NotNull(message = "Quantidade minima e obrigatoria.")
    @DecimalMin(value = "0.0", message = "Quantidade minima nao pode ser negativa.")
    BigDecimal minimumQuantity
) {}
