package com.serveflow.web.dto.menu;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record MenuItemSelectionDTO(
    @NotNull(message = "ID do item do menu e obrigatorio.")
    UUID menuItemId,

    @Min(value = 1, message = "Quantidade deve ser ao menos 1.")
    int quantity,

    @Size(max = 300, message = "Observacao deve ter no maximo 300 caracteres.")
    String observation
) {}
