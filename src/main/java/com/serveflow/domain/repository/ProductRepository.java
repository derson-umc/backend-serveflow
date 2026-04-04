package com.serveflow.domain.repository;

import com.serveflow.domain.model.product.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Product findById(UUID id);
    List<Product> findAllActive();
    List<Product> saveAll(List<Product> products);
    void deactivate(UUID id);
}
