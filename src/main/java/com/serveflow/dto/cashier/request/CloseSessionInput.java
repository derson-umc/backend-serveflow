package com.serveflow.dto.cashier.request;

import jakarta.validation.constraints.NotBlank;

public record CloseSessionInput(

        @NotBlank(message = "closedBy is required")
        String closedBy,

        String closingObservation
) {}
