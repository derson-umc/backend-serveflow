package com.serveflow.dto.cashier.request;

import com.serveflow.model.financial.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CashMovementInput(

        @NotNull(message = "type is required")
        TransactionType type,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "description is required")
        String description,

        String category,

        @NotBlank(message = "performedBy is required")
        String performedBy
) {}
