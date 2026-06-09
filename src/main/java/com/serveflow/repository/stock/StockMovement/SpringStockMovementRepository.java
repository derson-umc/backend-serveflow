package com.serveflow.repository.stock.stockmovement;

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
                si.name             AS insumo,
                si.unit             AS unidade,
                COALESCE(SUM(CASE
                    WHEN sm.type = 'ENTRY' THEN sm.quantity
                    WHEN sm.type = 'ADJUSTMENT' AND sm.balance_after > sm.balance_before
                         THEN (sm.balance_after - sm.balance_before)
                    ELSE 0
                END), 0)            AS totalEntradas,
                COALESCE(SUM(CASE
                    WHEN sm.type IN ('EXIT', 'ORDER_CONSUMPTION', 'LOSS') THEN sm.quantity
                    WHEN sm.type = 'ADJUSTMENT' AND sm.balance_before > sm.balance_after
                         THEN (sm.balance_before - sm.balance_after)
                    ELSE 0
                END), 0)            AS totalSaidas,
                si.current_quantity AS saldoAtual
            FROM stock_items si
            LEFT JOIN stock_movements sm ON si.id_stock_item = sm.stock_item_id
            WHERE si.status = 'ACTIVE'
            GROUP BY si.id_stock_item, si.name, si.unit, si.current_quantity
            ORDER BY si.name ASC
            """, nativeQuery = true)
    List<StockConsolidatedRow> findConsolidatedReport();
}
