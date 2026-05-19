package com.serveflow.dto.product.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductInput(

        @NotBlank(groups = OnCreate.class, message = "Nome do produto é obrigatório.")
        @Size(min = 2, max = 120, message = "Nome deve ter entre 2 e 120 caracteres.")
        String name,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
        String description,

        @NotBlank(groups = OnCreate.class, message = "Categoria é obrigatória.")
        @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres.")
        String category,

        @NotBlank(groups = OnCreate.class, message = "Marca é obrigatória.")
        @Size(max = 80, message = "Marca deve ter no máximo 80 caracteres.")
        String brand,

        @NotNull(groups = OnCreate.class, message = "Preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero.")
        BigDecimal price,

        @NotBlank(groups = OnCreate.class, message = "Porção é obrigatória.")
        String portion,

        @Size(max = 2048, message = "URL da imagem deve ter no máximo 2048 caracteres.")
        String imageUrl,

        Boolean requiresTechnicalSheet
) {}
