package com.serveflow.web.controller.product;

import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.facade.product.finder.ProductFinder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class FindProductById {

    private ProductFinder facade;

    public FindProductById(ProductFinder facade) {
        this.facade = facade;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductOutput> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(facade.findById(id));
    }

}
