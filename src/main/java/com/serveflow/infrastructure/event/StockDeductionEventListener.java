package com.serveflow.infrastructure.event;

import com.serveflow.application.usecase.ConfirmOrderStockDeductionUseCase;
import com.serveflow.domain.event.OrderConfirmedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StockDeductionEventListener {

    private final ConfirmOrderStockDeductionUseCase useCase;

    public StockDeductionEventListener(ConfirmOrderStockDeductionUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Escuta o evento de confirmação do pedido no mesmo contexto transacional.
     * Se a baixa de estoque falhar (estoque insuficiente), a confirmação do pedido
     * será revertida por rollback da transação.
     */
    @EventListener
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        useCase.execute(event);
    }
}
