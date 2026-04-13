package com.serveflow.web.controller.order;

import com.serveflow.web.dto.order.response.OrderOutput;
import com.serveflow.web.facade.OrderWebFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class FindOrdersByStatus {

    private final OrderWebFacade facade;

    public FindOrdersByStatus(OrderWebFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderOutput>> handle(@PathVariable String status) {
        return ResponseEntity.ok(facade.findByStatus(status));
    }
}
