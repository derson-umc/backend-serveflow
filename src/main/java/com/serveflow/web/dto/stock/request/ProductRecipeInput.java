package com.serveflow.web.dto.stock.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record ProductRecipeInput(
        @NotNull(message = "ID do produto é obrigatório.")
        UUID productId,

        @NotBlank(message = "Nome do produto é obrigatório.")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.")
        String productName,

        @NotEmpty(message = "Ficha técnica deve conter ao menos um ingrediente.")
        @Valid
        List<ProductRecipeInput> ingredients
) {}
