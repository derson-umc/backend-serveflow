package com.serveflow.Dto.Order.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Payload para criação de pedido via PDV (garçom)")
public record WaiterOrderInput(

        @Schema(description = "Número da mesa. Nulo para pedidos no balcão.", example = "12")
        Integer tableNumber,

        @Schema(description = "ID do garçom responsável.", example = "45")
        Long waiterId,

        @Schema(description = "Observação geral do pedido.", example = "Cliente alérgico a glúten")
        String observation,

        @NotEmpty(message = "Pedido deve conter ao menos um item.")
        @Valid
        @Schema(description = "Itens selecionados pelo garçom.")
        List<WaiterItemInput> items
) {
        @Schema(description = "Item selecionado no PDV")
        public record WaiterItemInput(

                @NotNull(message = "ID do produto é obrigatório.")
                @Schema(description = "ID do produto no catálogo.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
                UUID productId,

                @NotNull(message = "Quantidade é obrigatória.")
                @Min(value = 1, message = "Quantidade deve ser ao menos 1.")
                @Schema(description = "Quantidade do item.", example = "2")
                Integer quantity,

                @Schema(description = "Observação específica do item.", example = "Sem cebola")
                String observation
        ) {}
}
