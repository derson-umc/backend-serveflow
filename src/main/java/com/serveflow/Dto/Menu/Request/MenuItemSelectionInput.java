package com.serveflow.Dto.Menu.Request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record MenuItemSelectionInput(

        @NotNull(message = "ID do item do menu é obrigatório.")
        UUID menuItemId,

        @Min(value = 1, message = "Quantidade deve ser ao menos 1.")
        int quantity,

        @Size(max = 300, message = "Observação deve ter no máximo 300 caracteres.")
        String observation
) {}
