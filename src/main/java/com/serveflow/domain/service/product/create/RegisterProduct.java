package com.serveflow.domain.service.product.create;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class RegisterProduct {
    private final ProductRepository repository;

    public RegisterProduct(ProductRepository repository) {
        this.repository = repository;
    }

    public Product execute(Product product) {
        return repository.save(product);
    }
}