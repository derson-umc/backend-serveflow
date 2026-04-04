package com.serveflow.domain.service;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public List<Product> createBatch(List<Product> products) {
        return productRepository.saveAll(products);
    }

    public Product findById(UUID id) {
        return productRepository.findById(id);
    }

    public List<Product> findAllActive() {
        return productRepository.findAllActive();
    }

    public Product update(UUID id, Product updatedData) {
        Product existing = productRepository.findById(id);
        existing.update(
                updatedData.getName(),
                updatedData.getDescription(),
                updatedData.getCategory(),
                updatedData.getBrand(),
                updatedData.getPrice(),
                updatedData.getPortion()
        );
        return productRepository.save(existing);
    }

    public void deactivate(UUID id) {
        productRepository.deactivate(id);
    }
}
