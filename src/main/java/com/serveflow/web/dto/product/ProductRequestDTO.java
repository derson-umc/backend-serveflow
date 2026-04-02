package com.serveflow.web.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequestDTO(

        @NotBlank(message = "Nome do produto e obrigatorio")
        @Size(min = 2, max = 120, message = "Nome deve ter entre 2 e 120 caracteres")
        String name,

        @Size(max = 500, message = "Descricao deve ter no maximo 500 caracteres")
        String description,

        @NotBlank(message = "Categoria e obrigatoria")
        @Size(max = 100, message = "Categoria deve ter no maximo 100 caracteres")
        String category,

        @NotBlank(message = "Marca e obrigatoria")
        @Size(max = 80, message = "Marca deve ter no maximo 80 caracteres")
        String brand,

        @NotNull(message = "Preco e obrigatorio")
        @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero")
        BigDecimal price,

        @NotBlank(message = "Porcao e obrigatoria")
        String portion

) {}
