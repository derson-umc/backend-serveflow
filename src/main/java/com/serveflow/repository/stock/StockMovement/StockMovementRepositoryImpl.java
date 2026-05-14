package com.serveflow.repository.stock.StockMovement;

import com.serveflow.model.stock.MovementType;
import com.serveflow.model.stock.StockMovement;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class StockMovementRepositoryImpl implements StockMovementRepository {

    private final SpringStockMovementRepository springRepository;

    public StockMovementRepositoryImpl(SpringStockMovementRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    @Transactional
    public StockMovement save(StockMovement movement) {
        return toDomain(springRepository.save(toEntity(movement)));
    }

    @Override
    public List<StockMovement> findByStockItemId(UUID stockItemId) {
        return springRepository.findByStockItemId(stockItemId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<StockMovement> findByReferenceId(UUID referenceId) {
        return springRepository.findByReferenceId(referenceId).stream().map(this::toDomain).toList();
    }

    private StockMovement toDomain(StockMovementEntity e) {
        return new StockMovement(e.getIdMovement(), e.getStockItemId(),
                MovementType.valueOf(e.getType().name()),
                e.getQuantity(), e.getReason(), e.getReferenceId(), e.getCreatedAt());
    }

    private StockMovementEntity toEntity(StockMovement m) {
        StockMovementEntity e = new StockMovementEntity();
        e.setIdMovement(m.getId());
        e.setStockItemId(m.getStockItemId());
        e.setType(MovementType.valueOf(m.getType().name()));
        e.setQuantity(m.getQuantity());
        e.setReason(m.getReason());
        e.setReferenceId(m.getReferenceId());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }
}
