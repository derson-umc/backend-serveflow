package com.serveflow.dto.menu.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.util.List;

public record MenuInput(

        @NotBlank(message = "Nome do menu é obrigatório.")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
        String name,

        @NotEmpty(message = "Menu deve conter ao menos um item.")
        @Valid
        List<MenuItemInput> items,

        DayOfWeek dayOfWeek,

        String shift
) {}
