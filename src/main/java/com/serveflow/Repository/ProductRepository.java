package com.serveflow.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.serveflow.Model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>{
    List<Product> findAllByActiveTrue();
}
