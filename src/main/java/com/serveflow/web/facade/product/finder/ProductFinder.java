package com.serveflow.web.facade.product.finder;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.service.product.find.ListActiveProducts;
import com.serveflow.domain.service.product.find.RetrieveProduct;
import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.mapper.ProductWebMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProductFinder {

    private final RetrieveProduct retrieveProduct;
    private final ListActiveProducts listActiveProducts;
    private final ProductWebMapper mapper;

    public ProductFinder(RetrieveProduct retrieveProduct,
                         ListActiveProducts listActiveProducts,
                         ProductWebMapper mapper) {
        this.retrieveProduct = retrieveProduct;
        this.listActiveProducts = listActiveProducts;
        this.mapper = mapper;
    }

    public ProductOutput findById(UUID id) {
        Product product = retrieveProduct.execute(id);
        return mapper.toResponse(product);
    }

    public List<ProductOutput> listAllActive() {
        List<Product> products = listActiveProducts.execute();
        return mapper.toResponseList(products);
    }
}
