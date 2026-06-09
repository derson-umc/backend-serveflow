package com.serveflow.repository.stock.stockalert;

import com.serveflow.model.stock.StockAlert;

import java.util.List;
import java.util.UUID;

public interface StockAlertRepository {
    StockAlert save(StockAlert alert);
    StockAlert findById(UUID id);
    List<StockAlert> findAllActive();
    boolean existsActiveByStockItemId(UUID stockItemId);
}
