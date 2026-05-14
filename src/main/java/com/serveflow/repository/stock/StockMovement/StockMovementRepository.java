package com.serveflow.repository.stock.StockMovement;

import com.serveflow.model.stock.StockMovement;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    List<StockMovement> findByStockItemId(UUID stockItemId);
    List<StockMovement> findByReferenceId(UUID referenceId);
}
