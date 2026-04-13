package com.serveflow.data.mapper.product;

import com.serveflow.data.entity.product.ProductEntity;
import com.serveflow.domain.model.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

    public Product toDomain(ProductEntity entity) {
        return new Product(
                entity.getIdProduct(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getBrand(),
                entity.getPrice(),
                entity.getPortion(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    public ProductEntity toEntity(Product product) {
        var entity = new ProductEntity();
        entity.setIdProduct(product.getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setCategory(product.getCategory());
        entity.setBrand(product.getBrand());
        entity.setPrice(product.getPrice());
        entity.setPortion(product.getPortion());
        entity.setActive(product.isActive());
        entity.setCreatedAt(product.getCreatedAt());
        return entity;
    }

    public List<Product> toDomainList(List<ProductEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }
}
