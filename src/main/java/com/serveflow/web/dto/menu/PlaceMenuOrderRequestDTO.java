package com.serveflow.web.dto.menu;

import com.serveflow.web.dto.address.AddressRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record PlaceMenuOrderRequestDTO(
    @NotNull(message = "ID do menu e obrigatorio.")
    UUID menuId,

    @NotBlank(message = "Nome do cliente e obrigatorio.")
    @Size(max = 150, message = "Nome deve ter no maximo 150 caracteres.")
    String customerName,

    @NotNull(message = "Tipo do pedido e obrigatorio.")
    String type,

    @Valid
    AddressRequestDTO address,

    @Size(max = 500, message = "Observacao deve ter no maximo 500 caracteres.")
    String observation,

    @NotEmpty(message = "Selecione ao menos um item do menu.")
    @Valid
    List<MenuItemSelectionDTO> selections
) {}
