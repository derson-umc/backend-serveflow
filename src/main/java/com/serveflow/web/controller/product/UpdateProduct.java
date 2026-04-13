package com.serveflow.web.controller.product;

import com.serveflow.web.dto.product.request.ProductInput;
import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.facade.product.editor.ProductEditor;
import com.serveflow.web.validation.product.OnUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class UpdateProduct {

    private ProductEditor facade;

    public UpdateProduct(ProductEditor pf) {
        this.facade = pf;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductOutput> handle(
            @PathVariable UUID id,
            @RequestBody @Validated(OnUpdate.class) ProductInput request) {
        return ResponseEntity.ok(facade.update(id, request));
    }

    }

