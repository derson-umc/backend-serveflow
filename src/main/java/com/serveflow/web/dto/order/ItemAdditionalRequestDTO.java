package com.serveflow.web.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemAdditionalRequestDTO(

        @NotBlank(message = "Nome do adicional e obrigatório!")
        String name,

        @NotNull(message = "Quantidade do adicional e obrigatória")
        @Min(value = 1, message = "Quantidade do adicional deve ser no minimo 1")
        Integer quantity,

        @NotNull(message = "Preco unitario do adicional e obrigatório")
        @DecimalMin(value = "0.01", message = "Preço unitário do adicional deve ser maior que zero!")
        BigDecimal unitPrice
) {}
