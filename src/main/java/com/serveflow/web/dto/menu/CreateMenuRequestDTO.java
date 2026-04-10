package com.serveflow.web.dto.menu;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateMenuRequestDTO(
    @NotBlank(message = "Nome do menu e obrigatorio.")
    @Size(max = 100, message = "Nome deve ter no maximo 100 caracteres.")
    String name,

    @NotEmpty(message = "Menu deve conter ao menos um item.")
    @Valid
    List<CreateMenuItemDTO> items
) {}
