package com.serveflow.web.controller.product;

import com.serveflow.web.dto.product.request.ProductInput;
import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.facade.product.creator.ProductCreator;
import com.serveflow.web.validation.product.OnCreate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class CreateProduct {

    private final ProductCreator facade;

    public CreateProduct(ProductCreator facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<ProductOutput> handle(
            @RequestBody @Validated(OnCreate.class) ProductInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.create(request));
    }
}