package com.serveflow.repository.stock.StockAlert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringStockAlertRepository extends JpaRepository<StockAlertEntity, UUID> {
    boolean existsByStockItemIdAndResolvedFalse(UUID stockItemId);
    List<StockAlertEntity> findAllByResolvedFalse();
    List<StockAlertEntity> findAllByStockItemId(UUID stockItemId);
}
