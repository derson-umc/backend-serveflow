package com.serveflow.dto.menu.request;

import jakarta.validation.constraints.NotNull;

public record UpdateAvailabilityInput(
        @NotNull(message = "O campo 'available' é obrigatório.")
        Boolean available
) {}
