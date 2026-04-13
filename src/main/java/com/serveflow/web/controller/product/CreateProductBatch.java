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
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class CreateProductBatch {

    private final ProductCreator facade;

    public CreateProductBatch(ProductCreator facade) {
        this.facade = facade;
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductOutput>> createBatch(
            @RequestBody @Validated(OnCreate.class) List<ProductInput> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.createBatch(requests));
    }
}
