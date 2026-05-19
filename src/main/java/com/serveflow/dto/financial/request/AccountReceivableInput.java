package com.serveflow.dto.financial.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountReceivableInput(

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Due date is required")
        @FutureOrPresent(message = "Due date must be today or in the future")
        LocalDate dueDate,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        String category,
        UUID sourceOrderId
) {}
