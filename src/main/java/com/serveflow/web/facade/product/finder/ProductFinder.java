package com.serveflow.web.facade.product.finder;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.service.product.find.ListActiveProducts;
import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.mapper.ProductWebMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductFinder {

    private final ListActiveProducts listActiveProducts;
    private final ProductWebMapper mapper;

    public ProductFinder(ListActiveProducts listActiveProducts,
                         ProductWebMapper mapper) {
        this.listActiveProducts = listActiveProducts;
        this.mapper = mapper;
    }

    public List<ProductOutput> listAllActive() {
        List<Product> products = listActiveProducts.execute();
        return mapper.toResponseList(products);
    }
}
