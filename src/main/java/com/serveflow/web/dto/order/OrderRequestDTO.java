package com.serveflow.web.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO(

        @NotBlank(message = "Nome do cliente e obrigatorio")
        String customerName,

        @Valid
        AddressRequestDTO address,

        @NotNull(message = "Tipo do pedido e obrigatorio (DELIVERY ou LOCAL)")
        String type,

        String observation,

        @NotEmpty(message = "Pedido deve conter ao menos um item")
        @Valid
        List<OrderItemRequestDTO> items
) {}
