package com.serveflow.data.repository;

import com.serveflow.data.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringProductRepository extends JpaRepository<ProductEntity, UUID> {

    List<ProductEntity> findAllByActiveTrue();
}
