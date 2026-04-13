package com.serveflow.domain.service.product.delete;

import com.serveflow.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class DeactivateProduct {
    private final ProductRepository repository;

    public DeactivateProduct(ProductRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID id) {
        repository.deactivate(id);
    }
}