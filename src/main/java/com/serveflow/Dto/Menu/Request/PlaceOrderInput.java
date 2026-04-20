package com.serveflow.Dto.Menu.Request;

import com.serveflow.Dto.Order.Request.AddressInput;
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

        @NotEmpty(message = "Selecione ao menos um item do menu.")
        @Valid
        List<MenuItemSelectionInput> selections
) {}
