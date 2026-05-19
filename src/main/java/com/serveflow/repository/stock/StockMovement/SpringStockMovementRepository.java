package com.serveflow.repository.stock.StockMovement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SpringStockMovementRepository extends JpaRepository<StockMovementEntity, UUID>,
        JpaSpecificationExecutor<StockMovementEntity> {

    List<StockMovementEntity> findAllByOrderByCreatedAtDesc();
    List<StockMovementEntity> findByStockItemIdOrderByCreatedAtDesc(UUID stockItemId);
    List<StockMovementEntity> findByReferenceId(UUID referenceId);

    @Query(value = """
            SELECT
                si.name                AS insumo,
                si.unit                AS unidade,
                COALESCE(SUM(CASE WHEN sm.type = 'ENTRY' THEN sm.quantity ELSE 0 END), 0)
                                       AS totalEntradas,
                COALESCE(SUM(CASE WHEN sm.type IN ('EXIT', 'ORDER_CONSUMPTION') THEN sm.quantity ELSE 0 END), 0)
                                       AS totalSaidas,
                si.current_quantity    AS saldoAtual
            FROM stock_movements sm
            INNER JOIN stock_items si ON si.id_stock_item = sm.stock_item_id
            GROUP BY si.id_stock_item, si.name, si.unit, si.current_quantity
            ORDER BY totalSaidas DESC
            """, nativeQuery = true)
    List<StockConsolidatedRow> findConsolidatedReport();
}
