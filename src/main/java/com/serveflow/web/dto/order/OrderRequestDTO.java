package com.serveflow.web.dto.order;

import com.serveflow.web.dto.address.AddressRequestDTO;
import com.serveflow.web.validation.ValidDeliveryAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@ValidDeliveryAddress
public record OrderRequestDTO(

        @NotBlank(message = "Nome do cliente e obrigatorio.")
        String customerName,

        @Valid
        AddressRequestDTO address,

        @NotNull(message = "Tipo do pedido e obrigatorio. (DELIVERY ou LOCAL)")
        String type,

        String observation,

        @NotEmpty(message = "Pedido deve conter ao menos um item.")
        @Valid
        List<OrderItemRequestDTO> items
) {}
