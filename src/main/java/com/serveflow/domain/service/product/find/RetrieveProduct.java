package com.serveflow.domain.service.product.find;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class RetrieveProduct {
    private final ProductRepository repository;

    public RetrieveProduct(ProductRepository repository) {
        this.repository = repository;
    }

    public Product execute(UUID id) {
        return repository.findById(id);
    }
}