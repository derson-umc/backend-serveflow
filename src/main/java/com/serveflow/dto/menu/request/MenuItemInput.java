package com.serveflow.dto.menu.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemInput(

        @NotNull(message = "ID do produto é obrigatório.")
        UUID productId,

        @NotBlank(message = "Nome do item é obrigatório.")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.")
        String name,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
        String description,

        @NotNull(message = "Preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero.")
        BigDecimal price
) {}
