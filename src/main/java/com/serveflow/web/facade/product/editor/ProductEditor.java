package com.serveflow.web.facade.product.editor;

import com.serveflow.domain.model.product.Product;
import com.serveflow.domain.service.product.delete.DeactivateProduct;
import com.serveflow.domain.service.product.update.ChangeProduct;
import com.serveflow.web.dto.product.request.ProductInput;
import com.serveflow.web.dto.product.response.ProductOutput;
import com.serveflow.web.mapper.ProductWebMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductEditor {

    private final ChangeProduct changeProduct;
    private final DeactivateProduct deactivateProduct;
    private final ProductWebMapper mapper;

    public ProductEditor(ChangeProduct changeProduct,
                         DeactivateProduct deactivateProduct,
                         ProductWebMapper mapper) {
        this.changeProduct = changeProduct;
        this.deactivateProduct = deactivateProduct;
        this.mapper = mapper;
    }

    public ProductOutput update(UUID id, ProductInput request) {
        Product domain = mapper.toDomain(request);
        Product updated = changeProduct.execute(id, domain);
        return mapper.toResponse(updated);
    }

    public void deactivate(UUID id) {
        deactivateProduct.execute(id);
    }
}
