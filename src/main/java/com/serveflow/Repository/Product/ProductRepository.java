package com.serveflow.Repository.Product;

import com.serveflow.Exception.Product.ProductNotFound;
import com.serveflow.Model.Product.Product;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class ProductRepository {

    private final SpringProductRepository springRepository;

    public ProductRepository(SpringProductRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Transactional
    public Product save(Product product) {
        boolean isNew = product.getVersion() == null;
        ProductEntity entity;

        if (isNew) {
            entity = toEntity(product);
        } else {
            entity = springRepository.findById(product.getId())
                    .orElseThrow(() -> new ProductNotFound(product.getId()));
            updateEntity(entity, product);
        }

        return toDomain(springRepository.save(entity));
    }

    @Transactional
    public List<Product> saveAll(List<Product> products) {
        return springRepository.saveAll(products.stream().map(this::toEntity).toList())
                .stream().map(this::toDomain).toList();
    }

    public Product findById(UUID id) {
        return springRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new ProductNotFound(id));
    }

    public List<Product> findAllActive() {
        return springRepository.findAllByActiveTrue().stream().map(this::toDomain).toList();
    }

    @Transactional
    public void deactivate(UUID id) {
        ProductEntity entity = springRepository.findById(id)
                .orElseThrow(() -> new ProductNotFound(id));
        entity.setActive(false);
        springRepository.save(entity);
    }

    private Product toDomain(ProductEntity e) {
        return Product.builder()
                .id(e.getIdProduct())
                .name(e.getName())
                .description(e.getDescription())
                .category(e.getCategory())
                .brand(e.getBrand())
                .price(e.getPrice())
                .portion(e.getPortion())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .version(e.getVersion())
                .build();
    }

    private ProductEntity toEntity(Product p) {
        ProductEntity entity = new ProductEntity();
        entity.setIdProduct(p.getId());
        entity.setName(p.getName());
        entity.setDescription(p.getDescription());
        entity.setCategory(p.getCategory());
        entity.setBrand(p.getBrand());
        entity.setPrice(p.getPrice());
        entity.setPortion(p.getPortion());
        entity.setActive(p.isActive());
        entity.setCreatedAt(p.getCreatedAt());
        return entity;
    }

    private void updateEntity(ProductEntity entity, Product p) {
        entity.setName(p.getName());
        entity.setDescription(p.getDescription());
        entity.setCategory(p.getCategory());
        entity.setBrand(p.getBrand());
        entity.setPrice(p.getPrice());
        entity.setPortion(p.getPortion());
        entity.setActive(p.isActive());
    }
}
