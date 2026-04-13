package com.serveflow.domain.service.product.create;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RegisterProducts {
    private final ProductRepository repository;

    public RegisterProducts(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> execute(List<Product> products) {
        return repository.saveAll(products);
    }
}