package com.serveflow.dto.cashier.request;

import jakarta.validation.constraints.NotBlank;

public record SettleOrderInput(
        @NotBlank String paymentMethod
) {}
