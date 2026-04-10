package com.serveflow.web.dto.stock;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record CreateProductRecipeRequestDTO(
    @NotNull(message = "ID do produto e obrigatorio.")
    UUID productId,

    @NotBlank(message = "Nome do produto e obrigatorio.")
    @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres.")
    String productName,

    @NotEmpty(message = "Ficha tecnica deve conter ao menos um ingrediente.")
    @Valid
    List<CreateRecipeIngredientDTO> ingredients
) {}
