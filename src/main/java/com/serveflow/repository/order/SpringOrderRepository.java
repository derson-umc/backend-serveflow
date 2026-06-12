package com.serveflow.repository.order;

import com.serveflow.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SpringOrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.status = :status")
    List<OrderEntity> findByStatus(@Param("status") OrderStatus status);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.status = :status AND o.createdBy = :createdBy")
    List<OrderEntity> findByStatusAndCreatedBy(@Param("status") OrderStatus status, @Param("createdBy") String createdBy);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.createdBy = :createdBy")
    List<OrderEntity> findAllByCreatedBy(@Param("createdBy") String createdBy);

    @Query(value = """
            SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0)
            FROM orders o
            INNER JOIN order_items oi ON oi.id_order = o.id_order
            WHERE DATE(o.created_at) = CURRENT_DATE
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    BigDecimal revenueToday();

    @Query(value = """
            SELECT COUNT(o.id_order)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    long ordersToday();

    @Query(value = """
            SELECT COUNT(DISTINCT o.customer_name)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    long customersToday();

    @Query(value = """
            SELECT DATE(o.created_at)                             AS sale_date,
                   COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total
            FROM orders o
            INNER JOIN order_items oi ON oi.id_order = o.id_order
            WHERE DATE(o.created_at) >= :startDate
              AND o.status <> 'CANCELADO'
            GROUP BY DATE(o.created_at)
            ORDER BY sale_date ASC
            """, nativeQuery = true)
    List<Object[]> salesByDay(@Param("startDate") LocalDate startDate);

    @Query(value = """
            SELECT oi.product_name          AS name,
                   SUM(oi.quantity)         AS quantity
            FROM order_items oi
            INNER JOIN orders o ON o.id_order = oi.id_order
            WHERE o.created_at >= CURRENT_DATE - INTERVAL '29 days'
              AND o.status <> 'CANCELADO'
            GROUP BY oi.product_name
            ORDER BY quantity DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> topProducts();

    @Query(value = """
            SELECT oi.product_name                                        AS name,
                   SUM(oi.quantity)                                       AS quantity,
                   COALESCE(SUM(oi.quantity * oi.unit_price), 0)         AS revenue,
                   MAX(p.image_url)                                       AS image_url
            FROM order_items oi
            INNER JOIN orders o        ON o.id_order    = oi.id_order
            LEFT  JOIN products p      ON p.id_product  = oi.product_id
            WHERE DATE(o.created_at) >= :startDate
              AND o.status <> 'CANCELADO'
            GROUP BY oi.product_name
            ORDER BY quantity DESC
            LIMIT 8
            """, nativeQuery = true)
    List<Object[]> topProductsByPeriod(@Param("startDate") LocalDate startDate);

    @Query(value = """
            SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0)
            FROM orders o
            INNER JOIN order_items oi ON oi.id_order = o.id_order
            WHERE DATE(o.created_at) = CURRENT_DATE - 1
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    BigDecimal revenueYesterday();

    @Query(value = """
            SELECT COUNT(o.id_order)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE - 1
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    long ordersYesterday();

    @Query(value = """
            SELECT COUNT(DISTINCT o.customer_name)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE - 1
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    long customersYesterday();

    @Query(value = """
            SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0)
            FROM orders o
            INNER JOIN order_items oi ON oi.id_order = o.id_order
            WHERE DATE(o.created_at) = CURRENT_DATE - INTERVAL '7 days'
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    BigDecimal revenueSameDayLastWeek();

    @Query(value = """
            SELECT COUNT(o.id_order)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE - INTERVAL '7 days'
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    long ordersSameDayLastWeek();

    @Query(value = """
            SELECT COUNT(DISTINCT o.customer_name)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE - INTERVAL '7 days'
              AND o.status <> 'CANCELADO'
            """, nativeQuery = true)
    long customersSameDayLastWeek();

    @Query(value = """
            SELECT COUNT(o.id_order)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE
              AND o.status NOT IN ('ENTREGUE', 'CANCELADO')
            """, nativeQuery = true)
    long openOrdersToday();

    @Query(value = """
            SELECT
                cm.category                      AS method,
                COUNT(cm.id)                     AS orders_count,
                COALESCE(SUM(cm.amount), 0)      AS total
            FROM cash_movements cm
            WHERE cm.type = 'INCOME'
              AND cm.category IS NOT NULL
              AND cm.category NOT IN ('INCOME', 'EXPENSE')
              AND cm.created_at::date BETWEEN :startDate::date AND :endDate::date
            GROUP BY cm.category
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> cashierReportByPayment(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
