package com.serveflow.repository.stock.StockItem;

import com.serveflow.model.stock.StockItemStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringStockItemRepository extends JpaRepository<StockItemEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockItemEntity s WHERE s.idStockItem = :id")
    Optional<StockItemEntity> findByIdForUpdate(@Param("id") UUID id);

    List<StockItemEntity> findByStatus(StockItemStatus status);
}
