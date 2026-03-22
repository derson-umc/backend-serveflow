package com.serveflow.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.serveflow.DTO.ProductRequestDTO;
import com.serveflow.DTO.ProductResponseDTO;
import com.serveflow.Exception.ProductNotFoundException;
import com.serveflow.Model.Product;
import com.serveflow.Repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductResponseDTO> findAllActive() {
        return repository.findAllByActiveTrue()
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    public ProductResponseDTO findById(UUID id) {
        Product product = findProductOrThrow(id);
        return ProductResponseDTO.fromEntity(product);
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        Product product = Product.fromRequest(request);
        repository.save(product);
        return ProductResponseDTO.fromEntity(product);
    }

    @Transactional
    public ProductResponseDTO update(UUID id, ProductRequestDTO request) {
        Product product = findProductOrThrow(id);
        product.updateFrom(request);
        return ProductResponseDTO.fromEntity(product);
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = findProductOrThrow(id);
        product.deactivate();
    }

    // Método privado — evita repetir a busca + exceção
    private Product findProductOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

}
