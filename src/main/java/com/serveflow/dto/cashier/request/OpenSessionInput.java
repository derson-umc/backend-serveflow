package com.serveflow.dto.cashier.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record OpenSessionInput(

        @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
        BigDecimal initialBalance,

        String observation,

        @NotBlank(message = "openedBy is required")
        String openedBy
) {}
