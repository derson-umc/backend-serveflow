package com.serveflow.web.controller.order;

import com.serveflow.web.dto.order.request.OrderInput;
import com.serveflow.web.dto.order.response.OrderOutput;
import com.serveflow.web.facade.OrderWebFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class CreateOrder {

    private final OrderWebFacade facade;

    public CreateOrder(OrderWebFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<OrderOutput> handle(@RequestBody @Valid OrderInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.create(request));
    }
}
