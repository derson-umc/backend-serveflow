package com.serveflow.repository.cashier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SpringCashMovementRepository extends JpaRepository<CashMovementEntity, UUID> {

    List<CashMovementEntity> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    @Query(value = """
            SELECT
                COALESCE(cm.category, 'SEM_METODO') AS method,
                COUNT(cm.id)                         AS orders_count,
                COALESCE(SUM(cm.amount), 0)          AS total
            FROM cash_movements cm
            WHERE cm.type = 'INCOME'
              AND cm.created_at::date BETWEEN :startDate AND :endDate
            GROUP BY COALESCE(cm.category, 'SEM_METODO')
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> reportByPaymentMethod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}


