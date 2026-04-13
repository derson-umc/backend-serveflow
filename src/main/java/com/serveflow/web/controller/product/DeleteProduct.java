package com.serveflow.web.controller.product;

import com.serveflow.web.facade.ProductWebFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class DeleteProduct {

    private final ProductWebFacade facade;

    public DeleteProduct(ProductWebFacade facade) {
        this.facade = facade;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> handle(@PathVariable UUID id) {
        facade.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}