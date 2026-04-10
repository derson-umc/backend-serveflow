package com.serveflow.domain.repository;

import com.serveflow.domain.model.stock.StockItem;

import java.util.List;
import java.util.UUID;

public interface StockItemRepository {
    StockItem save(StockItem item);
    StockItem findById(UUID id);
    StockItem findByIdForUpdate(UUID id);
    List<StockItem> findAll();
}
