package com.serveflow.data.repository.product;

import com.serveflow.data.entity.product.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringProductRepository extends JpaRepository<ProductEntity, UUID> {

    List<ProductEntity> findAllByActiveTrue();
}
