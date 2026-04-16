package com.serveflow.data.repository.stock;

import com.serveflow.data.mapper.StockMapper;
import com.serveflow.domain.exception.StockItemNotFound;
import com.serveflow.domain.model.stock.StockItem;
import com.serveflow.domain.repository.StockItemRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class StockItemRepositoryImpl implements StockItemRepository {

    private final SpringStockItemRepository springRepository;
    private final StockMapper mapper;

    public StockItemRepositoryImpl(SpringStockItemRepository springRepository, StockMapper mapper) {
        this.springRepository = springRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public StockItem save(StockItem item) {
        var entity = springRepository.findById(item.getId())
                .map(existing -> mapper.updateEntity(existing, item))
                .orElseGet(() -> mapper.toEntity(item));

        return mapper.toDomain(springRepository.save(entity));
    }

    @Override
    public StockItem findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new StockItemNotFound(id));
    }

    @Override
    @Transactional
    public StockItem findByIdForUpdate(UUID id) {
        return springRepository.findByIdForUpdate(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new StockItemNotFound(id));
    }

    @Override
    public List<StockItem> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
