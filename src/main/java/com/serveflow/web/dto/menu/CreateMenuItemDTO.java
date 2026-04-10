package com.serveflow.web.dto.menu;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateMenuItemDTO(
    @NotNull(message = "ID do produto e obrigatorio.")
    UUID productId,

    @NotBlank(message = "Nome do item e obrigatorio.")
    @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres.")
    String name,

    @Size(max = 500, message = "Descricao deve ter no maximo 500 caracteres.")
    String description,

    @NotNull(message = "Preco e obrigatorio.")
    @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero.")
    BigDecimal price
) {}
