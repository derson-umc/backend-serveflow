package com.serveflow.service.product;

import com.serveflow.dto.product.request.ProductInput;
import com.serveflow.dto.product.response.ProductOutput;
import com.serveflow.model.product.Product;
import com.serveflow.model.product.ProductCategory;
import com.serveflow.repository.product.ProductRepository;
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

    public List<ProductOutput> findAll() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    @Transactional
    public ProductOutput update(UUID id, ProductInput request) {
        Product existing = repository.findById(id);
        Product patch = toDomain(request);
        if (request.active() == null) {
            patch = patch.toBuilder().active(existing.isActive()).build();
        }
        existing.update(patch);
        return toOutput(repository.save(existing));
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = repository.findById(id);
        if (product.isActive()) {
            repository.deactivate(id);
        } else {
            repository.hardDelete(id);
        }
    }

    @Transactional
    public ProductOutput toggleStatus(UUID id) {
        return toOutput(repository.toggleActive(id));
    }

    private Product toDomain(ProductInput dto) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name(dto.name())
                .description(dto.description())
                .category(dto.category())
                .brand(dto.brand())
                .price(dto.price())
                .portion(dto.portion())
                .imageUrl(dto.imageUrl())
                .requiresTechnicalSheet(dto.requiresTechnicalSheet() != null && dto.requiresTechnicalSheet())
                .active(dto.active() == null || dto.active())
                .productCategory(parseCategory(dto.productCategory()))
                .requiresHotPrep(dto.requiresHotPrep() != null && dto.requiresHotPrep())
                .build();
    }

    private ProductOutput toOutput(Product p) {
        return new ProductOutput(
                p.getId(), p.getName(), p.getDescription(),
                p.getCategory(), p.getBrand(), p.getPrice(), p.getPortion(),
                p.getImageUrl(), p.isActive(), p.isRequiresTechnicalSheet(), p.getCreatedAt(),
                p.getProductCategory() != null ? p.getProductCategory().name() : null,
                p.isRequiresHotPrep()
        );
    }

    private ProductCategory parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try { return ProductCategory.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
