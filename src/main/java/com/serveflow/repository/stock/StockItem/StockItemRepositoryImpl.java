package com.serveflow.repository.stock.StockItem;

import com.serveflow.exception.stock.StockItemNotFound;
import com.serveflow.model.stock.StockItem;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class StockItemRepositoryImpl implements StockItemRepository {

    private final SpringStockItemRepository springRepository;

    public StockItemRepositoryImpl(SpringStockItemRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    @Transactional
    public StockItem save(StockItem item) {
        StockItemEntity entity = springRepository.findById(item.getId())
                .map(e -> updateEntity(e, item))
                .orElseGet(() -> toEntity(item));
        return toDomain(springRepository.save(entity));
    }

    @Override
    public StockItem findById(UUID id) {
        return springRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new StockItemNotFound(id));
    }

    @Override
    @Transactional
    public StockItem findByIdForUpdate(UUID id) {
        return springRepository.findByIdForUpdate(id)
                .map(this::toDomain)
                .orElseThrow(() -> new StockItemNotFound(id));
    }

    @Override
    public List<StockItem> findAll() {
        return springRepository.findAll().stream().map(this::toDomain).toList();
    }

    // ── mapping ───────────────────────────────────────────────────────────────

    private StockItem toDomain(StockItemEntity e) {
        return new StockItem(e.getIdStockItem(), e.getName(), e.getUnit(),
                e.getCurrentQuantity(), e.getMinimumQuantity(), e.getCreatedAt(), e.getVersion());
    }

    private StockItemEntity toEntity(StockItem item) {
        StockItemEntity e = new StockItemEntity();
        e.setIdStockItem(item.getId());
        e.setVersion(item.getVersion());
        e.setName(item.getName());
        e.setUnit(item.getUnit());
        e.setCurrentQuantity(item.getCurrentQuantity());
        e.setMinimumQuantity(item.getMinimumQuantity());
        e.setCreatedAt(item.getCreatedAt());
        return e;
    }

    private StockItemEntity updateEntity(StockItemEntity e, StockItem item) {
        e.setName(item.getName());
        e.setUnit(item.getUnit());
        e.setCurrentQuantity(item.getCurrentQuantity());
        e.setMinimumQuantity(item.getMinimumQuantity());
        return e;
    }
}
