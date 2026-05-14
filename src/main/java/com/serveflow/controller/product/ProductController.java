package com.serveflow.controller.product;

import com.serveflow.dto.product.request.OnCreate;
import com.serveflow.dto.product.request.OnUpdate;
import com.serveflow.dto.product.request.ProductInput;
import com.serveflow.dto.product.response.ProductOutput;
import com.serveflow.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductOutput> create(
            @Validated(OnCreate.class) @RequestBody ProductInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductOutput>> createBatch(
            @Validated(OnCreate.class) @RequestBody List<ProductInput> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createBatch(requests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductOutput> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductOutput>> findAllActive() {
        return ResponseEntity.ok(productService.findAllActive());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductOutput> update(
            @PathVariable UUID id,
            @Validated(OnUpdate.class) @RequestBody ProductInput request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
