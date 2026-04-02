package com.serveflow.data.repository;

import com.serveflow.data.mapper.ProductMapper;
import com.serveflow.domain.exception.ProductNotFoundException;
import com.serveflow.domain.model.Product;
import com.serveflow.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class ProductRepositoryImpl implements ProductRepository {

    private final SpringProductRepository springRepository;
    private final ProductMapper mapper;

    public ProductRepositoryImpl(SpringProductRepository springRepository,
                                 ProductMapper mapper) {
        this.springRepository = springRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Product save(Product product) {
        var entity = mapper.toEntity(product);
        var saved = springRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public List<Product> saveAll(List<Product> products) {
        var entities = products.stream()
                .map(mapper::toEntity)
                .toList();
        return springRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Product findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public List<Product> findAllActive() {
        return mapper.toDomainList(springRepository.findAllByActiveTrue());
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        var entity = springRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        entity.setActive(false);
        springRepository.save(entity);
    }
}
