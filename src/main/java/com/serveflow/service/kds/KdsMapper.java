package com.serveflow.service.kds;

import com.serveflow.dto.kds.response.KdsItemOutput;
import com.serveflow.dto.kds.response.KdsOrderOutput;
import com.serveflow.dto.order.response.OrderOutput;
import org.springframework.stereotype.Component;

@Component
public class KdsMapper {

    public KdsOrderOutput toOutput(OrderOutput o) {
        return new KdsOrderOutput(
                o.id(),
                o.customerName(),
                o.type(),
                o.status(),
                o.comandaStatus(),
                o.createdAt(),
                o.items().stream()
                        .map(i -> new KdsItemOutput(
                                i.id(),
                                i.productName(),
                                i.quantity(),
                                i.observation(),
                                i.additionals().stream()
                                        .map(a -> a.name() + (a.quantity() > 1 ? " x" + a.quantity() : ""))
                                        .toList()
                        ))
                        .toList()
        );
    }
}
