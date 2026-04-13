package com.serveflow.web.mapper;

import com.serveflow.domain.model.product.Product;
import com.serveflow.web.dto.product.request.ProductInput;
import com.serveflow.web.dto.product.response.ProductOutput;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductWebMapper {

    public Product toDomain(ProductInput request) {
        return new Product(
                request.name(),
                request.description(),
                request.category(),
                request.brand(),
                request.price(),
                request.portion()
        );
    }

    public ProductOutput toResponse(Product product) {
        return new ProductOutput(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBrand(),
                product.getPrice(),
                product.getPortion()
        );
    }

    public List<ProductOutput> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .toList();
    }
}
