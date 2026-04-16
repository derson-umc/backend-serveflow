package com.serveflow.data.repository.stock;

import com.serveflow.data.mapper.StockMapper;
import com.serveflow.domain.exception.RecipeNotFound;
import com.serveflow.domain.model.stock.ProductRecipe;
import com.serveflow.domain.repository.ProductRecipeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class ProductRecipeRepositoryImpl implements ProductRecipeRepository {

    private final SpringProductRecipeRepository springRepository;
    private final StockMapper mapper;

    public ProductRecipeRepositoryImpl(SpringProductRecipeRepository springRepository, StockMapper mapper) {
        this.springRepository = springRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ProductRecipe save(ProductRecipe recipe) {
        var entity = springRepository.findById(recipe.getId())
                .map(existing -> mapper.updateEntity(existing, recipe))
                .orElseGet(() -> mapper.toEntity(recipe));

        return mapper.toDomain(springRepository.save(entity));
    }

    @Override
    public Optional<ProductRecipe> findByProductId(UUID productId) {
        return springRepository.findByProductId(productId)
                .map(mapper::toDomain);
    }

    @Override
    public ProductRecipe findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new RecipeNotFound(id));
    }

    @Override
    public List<ProductRecipe> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
