package com.serveflow.dto.menu.request;

import jakarta.validation.constraints.NotBlank;

public record RemoveMenuItemInput(
        @NotBlank(message = "Nome do cozinheiro é obrigatório.")
        String chefName
) {}
