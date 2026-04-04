package com.serveflow.web.controller;

import com.serveflow.domain.service.ProductService;
import com.serveflow.web.dto.product.ProductRequestDTO;
import com.serveflow.web.dto.product.ProductResponseDTO;
import com.serveflow.web.mapper.ProductWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductWebMapper mapper;

    public ProductController(ProductService productService, ProductWebMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(
            @RequestBody @Valid ProductRequestDTO request) {
        var created = productService.create(mapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductResponseDTO>> createBatch(
            @RequestBody @Valid List<ProductRequestDTO> requests) {
        var products = requests.stream()
                .map(mapper::toDomain)
                .toList();
        var created = productService.createBatch(products);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseList(created));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listAll() {
        return ResponseEntity.ok(mapper.toResponseList(productService.findAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(productService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid ProductRequestDTO request) {
        var updated = productService.update(id, mapper.toDomain(request));
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
