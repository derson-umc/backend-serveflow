package com.serveflow.web.facade;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.service.product.create.RegisterProduct;
import com.serveflow.domain.service.product.create.RegisterProducts;
import com.serveflow.domain.service.product.delete.DeactivateProduct;
import com.serveflow.domain.service.product.find.ListActiveProducts;
import com.serveflow.domain.service.product.find.RetrieveProduct;
import com.serveflow.domain.service.product.update.ChangeProduct;
import com.serveflow.web.dto.product.request.ProductInput;
import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.mapper.ProductWebMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProductWebFacade {

    private final RegisterProduct registerProduct;
    private final RegisterProducts registerProducts;
    private final RetrieveProduct retrieveProduct;
    private final ChangeProduct changeProduct;
    private final DeactivateProduct deactivateProduct;
    private final ListActiveProducts listActiveProducts;
    private final ProductWebMapper mapper;

    public ProductWebFacade(RegisterProduct registerProduct,
                            RegisterProducts registerProducts,
                            RetrieveProduct retrieveProduct,
                            ChangeProduct changeProduct,
                            DeactivateProduct deactivateProduct,
                            ListActiveProducts listActiveProducts,
                            ProductWebMapper mapper) {
        this.registerProduct = registerProduct;
        this.registerProducts = registerProducts;
        this.retrieveProduct = retrieveProduct;
        this.changeProduct = changeProduct;
        this.deactivateProduct = deactivateProduct;
        this.listActiveProducts = listActiveProducts;
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

    public ProductOutput findById(UUID id) {
        Product product = retrieveProduct.execute(id);
        return mapper.toResponse(product);
    }

    public ProductOutput update(UUID id, ProductInput request) {
        Product domain = mapper.toDomain(request);
        Product updated = changeProduct.execute(id, domain);
        return mapper.toResponse(updated);
    }

    public List<ProductOutput> listAllActive() {
        List<Product> products = listActiveProducts.execute();
        return mapper.toResponseList(products);
    }

    public void deactivate(UUID id) {
        deactivateProduct.execute(id);
    }
}
