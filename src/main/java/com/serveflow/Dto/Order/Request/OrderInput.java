package com.serveflow.Dto.Order.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@MustHaveValidAddress
public record OrderInput(

        @NotBlank(message = "Nome do cliente é obrigatório.")
        String customerName,

        @Valid
        AddressInput address,

        @NotNull(message = "Tipo do pedido é obrigatório. (DELIVERY ou LOCAL)")
        String type,

        String observation,

        @NotEmpty(message = "Pedido deve conter ao menos um item.")
        @Valid
        List<OrderItemInput> items
) {}
