package com.serveflow.domain.service.product.find;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListActiveProducts {
    private final ProductRepository repository;

    public ListActiveProducts(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> execute() {
        return repository.findAllActive();
    }
}