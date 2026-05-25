package com.serveflow.repository.stock.StockAlert;

import com.serveflow.exception.stock.StockAlertNotFoundException;
import com.serveflow.model.stock.StockAlert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class StockAlertRepositoryImpl implements StockAlertRepository {

    private final SpringStockAlertRepository springRepository;

    public StockAlertRepositoryImpl(SpringStockAlertRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    @Transactional
    public StockAlert save(StockAlert alert) {
        StockAlertEntity entity;
        if (alert.isResolved()) {
            entity = springRepository.findById(alert.getId())
                    .orElseGet(() -> toEntity(alert));
            entity.setResolved(true);
            entity.setResolvedAt(alert.getResolvedAt());
        } else {
            entity = toEntity(alert);
        }
        return toDomain(springRepository.save(entity));
    }

    @Override
    public StockAlert findById(UUID id) {
        return springRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new StockAlertNotFoundException(id));
    }

    @Override
    public List<StockAlert> findAllActive() {
        return springRepository.findAllByResolvedFalse().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsActiveByStockItemId(UUID stockItemId) {
        return springRepository.existsByStockItemIdAndResolvedFalse(stockItemId);
    }

    private StockAlert toDomain(StockAlertEntity e) {
        return new StockAlert(
                e.getIdAlert(), e.getStockItemId(), e.getStockItemName(),
                e.getCurrentQty(), e.getMinimumQty(),
                e.isResolved(), e.getCreatedAt(), e.getResolvedAt()
        );
    }

    private StockAlertEntity toEntity(StockAlert alert) {
        StockAlertEntity entity = new StockAlertEntity();
        entity.setIdAlert(alert.getId());
        entity.setStockItemId(alert.getStockItemId());
        entity.setStockItemName(alert.getStockItemName());
        entity.setCurrentQty(alert.getCurrentQuantity());
        entity.setMinimumQty(alert.getMinimumQuantity());
        entity.setResolved(alert.isResolved());
        entity.setCreatedAt(alert.getCreatedAt());
        entity.setResolvedAt(alert.getResolvedAt());
        return entity;
    }
}
