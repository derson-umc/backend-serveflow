package com.serveflow.Repository.Stock.StockMovement;

import com.serveflow.Model.Stock.StockMovement;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    List<StockMovement> findByStockItemId(UUID stockItemId);
    List<StockMovement> findByReferenceId(UUID referenceId);
}
