package com.serveflow.Dto.Order.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@MustHaveValidAddress
@Schema(description = "Pedido registrado pelo garçom. Suporta pedidos locais (mesa) e delivery (com endereço). Nome e preço dos produtos são resolvidos automaticamente a partir do catálogo.")
public record CreateOrderInput(

        @Schema(description = "Nome do cliente ou identificação da mesa", example = "Mesa 5")
        @NotBlank(message = "Nome do cliente é obrigatório.")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
        String customerName,

        @Schema(description = "Tipo do pedido", example = "LOCAL", allowableValues = {"LOCAL", "DELIVERY"})
        @NotNull(message = "Tipo do pedido é obrigatório.")
        String type,

        @Schema(description = "Número da mesa. Informar para pedidos locais.", example = "5")
        Integer tableNumber,

        @Schema(description = "ID do garçom responsável pelo pedido", example = "3")
        Long waiterId,

        @Schema(description = "Endereço de entrega. Obrigatório quando type = DELIVERY.")
        @Valid
        AddressInput address,

        @Schema(description = "Observação geral do pedido", example = "Sem cebola em todos os itens")
        @Size(max = 500)
        String observation,

        @Schema(description = "Itens do pedido. Ao menos um é obrigatório.")
        @NotEmpty(message = "Pedido deve conter ao menos um item.")
        @Valid
        List<OrderItemRequest> items
) {

        @Schema(description = "Item do pedido. Nome e preço são resolvidos pelo servidor a partir do ID do produto.")
        public record OrderItemRequest(

                @Schema(description = "ID do produto no catálogo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
                @NotNull(message = "ID do produto é obrigatório.")
                UUID productId,

                @Schema(description = "Quantidade do item", example = "2")
                @NotNull(message = "Quantidade é obrigatória.")
                @Min(value = 1, message = "Quantidade deve ser ao menos 1.")
                Integer quantity,

                @Schema(description = "Observação específica do item", example = "Ponto mal passado")
                String observation
        ) {}
}
