package com.serveflow.Dto.Order.Request;

import com.serveflow.Model.Order.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Forma de pagamento escolhida pelo cliente para confirmar o pedido.")
public record FinalizeOrderInput(

        @Schema(description = "Forma de pagamento", example = "PIX",
                allowableValues = {"CASH", "DEBIT_CARD", "CREDIT_CARD", "PIX"})
        @NotNull(message = "Forma de pagamento é obrigatória.")
        PaymentMethod paymentMethod
) {}
