package com.serveflow.web.controller.order;

import com.serveflow.web.dto.order.response.OrderOutput;
import com.serveflow.web.facade.OrderWebFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class SendOrder {

    private final OrderWebFacade facade;

    public SendOrder(OrderWebFacade facade) {
        this.facade = facade;
    }

    @PatchMapping("/{id}/send")
    public ResponseEntity<OrderOutput> handle(@PathVariable UUID id) {
        return ResponseEntity.ok(facade.sendForDelivery(id));
    }
}
