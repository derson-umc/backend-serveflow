package com.serveflow.dto.menu.request;

import com.serveflow.dto.order.request.AddressInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record PlaceOrderInput(

        @NotBlank(message = "Nome do cliente é obrigatório.")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
        String customerName,

        @NotNull(message = "Tipo do pedido é obrigatório.")
        String type,

        @Valid
        AddressInput address,

        @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres.")
        String observation,

        @Size(max = 30, message = "Número da mesa deve ter no máximo 30 caracteres.")
        String tableNumber,

        @NotEmpty(message = "Selecione ao menos um item do menu.")
        @Valid
        List<MenuItemSelectionInput> selections
) {}
