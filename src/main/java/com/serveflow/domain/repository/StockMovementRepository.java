package com.serveflow.domain.repository;

import com.serveflow.domain.model.stock.StockMovement;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    List<StockMovement> findByStockItemId(UUID stockItemId);
    List<StockMovement> findByReferenceId(UUID referenceId);
}
