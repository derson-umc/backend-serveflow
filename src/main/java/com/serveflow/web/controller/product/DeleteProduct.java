package com.serveflow.web.controller.product;

import com.serveflow.web.facade.product.editor.ProductEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class DeleteProduct {

    private final ProductEditor facade;

    public DeleteProduct(ProductEditor facade) {
        this.facade = facade;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> handle(@PathVariable UUID id) {
        facade.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}