package com.serveflow.web.mapper;

import com.serveflow.domain.model.Product;
import com.serveflow.web.dto.ProductRequestDTO;
import com.serveflow.web.dto.ProductResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductWebMapper {

    public Product toDomain(ProductRequestDTO request) {
        return new Product(
                request.name(),
                request.description(),
                request.category(),
                request.brand(),
                request.price(),
                request.portion()
        );
    }

    public ProductResponseDTO toResponse(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBrand(),
                product.getPrice(),
                product.getPortion()
        );
    }

    public List<ProductResponseDTO> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .toList();
    }
}
