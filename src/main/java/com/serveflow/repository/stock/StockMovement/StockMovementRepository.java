package com.serveflow.repository.stock.StockMovement;

import com.serveflow.dto.stock.response.StockConsolidatedOutput;
import com.serveflow.dto.stock.response.StockMovementsPageOutput;
import com.serveflow.model.stock.MovementType;
import com.serveflow.model.stock.StockMovement;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    List<StockMovement> findAll();
    List<StockMovement> findByStockItemId(UUID stockItemId);
    List<StockMovement> findByReferenceId(UUID referenceId);
    List<StockConsolidatedOutput> findConsolidatedReport();
    StockMovementsPageOutput findFiltered(UUID stockItemId, MovementType type, LocalDate start, LocalDate end, int page, int size);
}
