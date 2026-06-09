package com.serveflow.repository.stock.stockitem;

import com.serveflow.model.stock.StockItem;
import com.serveflow.model.stock.StockItemStatus;

import java.util.List;
import java.util.UUID;

public interface StockItemRepository {
    StockItem save(StockItem item);
    StockItem findById(UUID id);
    StockItem findByIdForUpdate(UUID id);
    List<StockItem> findAll();
    List<StockItem> findByStatus(StockItemStatus status);
}
