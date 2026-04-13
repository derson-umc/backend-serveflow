package com.serveflow.domain.service.product.update;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ChangeProduct {
    private final ProductRepository repository;
    private final ProductUpdater updater;

    public ChangeProduct(ProductRepository repository, ProductUpdater updater) {
        this.repository = repository;
        this.updater = updater;
    }

    @Transactional
    public Product execute(UUID id, Product updatedData) {
        Product existing = repository.findById(id);
        updater.updateData(existing, updatedData);
        return repository.save(existing);
    }
}