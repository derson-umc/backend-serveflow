package com.serveflow.Dto.Menu.Request;

import jakarta.validation.constraints.NotNull;

public record UpdateAvailabilityInput(
        @NotNull(message = "O campo 'available' é obrigatório.")
        Boolean available
) {}
