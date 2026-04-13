package com.serveflow.domain.service.product.update;

import com.serveflow.domain.model.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductUpdater {

    public void updateData(Product existing, Product newData) {
        if (newData.getName() != null) existing.setName(newData.getName());
        if (newData.getDescription() != null) existing.setDescription(newData.getDescription());
        if (newData.getCategory() != null) existing.setCategory(newData.getCategory());
        if (newData.getBrand() != null) existing.setBrand(newData.getBrand());
        if (newData.getPrice() != null) existing.setPrice(newData.getPrice());
        if (newData.getPortion() != null) existing.setPortion(newData.getPortion());
    }
}
