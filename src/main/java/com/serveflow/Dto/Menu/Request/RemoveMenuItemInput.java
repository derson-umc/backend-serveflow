package com.serveflow.Dto.Menu.Request;

import jakarta.validation.constraints.NotBlank;

public record RemoveMenuItemInput(
        @NotBlank(message = "Nome do cozinheiro é obrigatório.")
        String chefName
) {}
