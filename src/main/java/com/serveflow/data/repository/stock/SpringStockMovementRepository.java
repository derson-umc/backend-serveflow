package com.serveflow.data.repository.stock;

import com.serveflow.data.entity.stock.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringStockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {
    List<StockMovementEntity> findByStockItemId(UUID stockItemId);
    List<StockMovementEntity> findByReferenceId(UUID referenceId);
}
