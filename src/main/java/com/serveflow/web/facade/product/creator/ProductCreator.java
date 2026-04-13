package com.serveflow.web.facade.product.creator;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.service.product.create.RegisterProduct;
import com.serveflow.domain.service.product.create.RegisterProducts;
import com.serveflow.web.dto.product.request.ProductInput;
import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.mapper.ProductWebMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCreator {

    private final RegisterProduct registerProduct;
    private final RegisterProducts registerProducts;
    private final ProductWebMapper mapper;

    public ProductCreator(RegisterProduct registerProduct,
                          RegisterProducts registerProducts,
                          ProductWebMapper mapper) {
        this.registerProduct = registerProduct;
        this.registerProducts = registerProducts;
        this.mapper = mapper;
    }

    public ProductOutput create(ProductInput request) {
        Product domain = mapper.toDomain(request);
        Product created = registerProduct.execute(domain);
        return mapper.toResponse(created);
    }

    public List<ProductOutput> createBatch(List<ProductInput> requests) {
        List<Product> domains = requests.stream()
                .map(mapper::toDomain)
                .toList();

        List<Product> created = registerProducts.execute(domains);
        return mapper.toResponseList(created);
    }
}
