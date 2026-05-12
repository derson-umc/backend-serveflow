package com.serveflow.Service.Product;

import com.serveflow.Dto.Product.Request.ProductInput;
import com.serveflow.Dto.Product.Response.ProductOutput;
import com.serveflow.Model.Product.Product;
import com.serveflow.Repository.Product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductOutput create(ProductInput request) {
        return toOutput(repository.save(toDomain(request)));
    }

    @Transactional
    public List<ProductOutput> createBatch(List<ProductInput> requests) {
        return repository.saveAll(requests.stream().map(this::toDomain).toList())
                .stream().map(this::toOutput).toList();
    }

    public ProductOutput findById(UUID id) {
        return toOutput(repository.findById(id));
    }

    public List<ProductOutput> findAllActive() {
        return repository.findAllActive().stream().map(this::toOutput).toList();
    }

    @Transactional
    public ProductOutput update(UUID id, ProductInput request) {
        Product existing = repository.findById(id);
        existing.update(toDomain(request));
        return toOutput(repository.save(existing));
    }

    @Transactional
    public void deactivate(UUID id) {
        repository.deactivate(id);
    }

    private Product toDomain(ProductInput dto) {
        return Product.builder()
                .name(dto.name())
                .description(dto.description())
                .category(dto.category())
                .brand(dto.brand())
                .price(dto.price())
                .portion(dto.portion())
                .build();
    }

    private ProductOutput toOutput(Product p) {
        return new ProductOutput(
                p.getId(), p.getName(), p.getDescription(),
                p.getCategory(), p.getBrand(), p.getPrice(), p.getPortion(),
                p.isActive(), p.getCreatedAt()
        );
    }
}
