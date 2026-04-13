package com.serveflow.web.controller.product;

import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.facade.product.finder.ProductFinder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ListProducts {

    private final ProductFinder facade;

    public ListProducts(ProductFinder facade) {
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<List<ProductOutput>> handle() {
        return ResponseEntity.ok(facade.listAllActive());
    }
}
