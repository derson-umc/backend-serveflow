package com.serveflow.infrastructure.event;

import com.serveflow.application.usecase.ProcessStockOut;
import com.serveflow.domain.event.OrderConfirmedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotifyStockDeduction {

    private final ProcessStockOut useCase;

    public NotifyStockDeduction(ProcessStockOut useCase) {
        this.useCase = useCase;
    }

    @EventListener
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        useCase.execute(event);
    }
}
