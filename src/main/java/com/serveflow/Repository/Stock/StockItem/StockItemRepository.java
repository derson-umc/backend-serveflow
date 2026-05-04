package com.serveflow.Repository.Stock.StockItem;

import com.serveflow.Model.Stock.StockItem;

import java.util.List;
import java.util.UUID;

public interface StockItemRepository {
    StockItem save(StockItem item);
    StockItem findById(UUID id);
    StockItem findByIdForUpdate(UUID id);
    List<StockItem> findAll();
}
