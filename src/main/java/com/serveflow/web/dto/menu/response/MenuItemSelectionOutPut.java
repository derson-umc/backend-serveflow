package com.serveflow.web.dto.menu.response;

import com.serveflow.web.dto.address.request.AddressInput;
import com.serveflow.web.dto.menu.request.MenuItemSelectionInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record MenuItemSelectionOutPut(
    @NotNull(message = "ID do menu é obrigatório.")
    UUID menuId,

    @NotBlank(message = "Nome do cliente é obrigatório.")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
    String customerName,

    @NotNull(message = "Tipo do pedido é obrigatório.")
    String type,

    @Valid
    AddressInput address,

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres.")
    String observation,

    @NotEmpty(message = "Selecione ao menos um item do menu.")
    @Valid
    List<MenuItemSelectionInput> selections
) {}
