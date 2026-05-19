package com.serveflow.repository.order;

import com.serveflow.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SpringOrderRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findByStatus(OrderStatus status);

    @Query(value = """
            SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0)
            FROM orders o
            INNER JOIN order_items oi ON oi.id_order = o.id_order
            WHERE DATE(o.created_at) = CURRENT_DATE
              AND o.status <> 'CANCELLED'
            """, nativeQuery = true)
    BigDecimal revenueToday();

    @Query(value = """
            SELECT COUNT(o.id_order)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE
              AND o.status <> 'CANCELLED'
            """, nativeQuery = true)
    long ordersToday();

    @Query(value = """
            SELECT COUNT(DISTINCT o.customer_name)
            FROM orders o
            WHERE DATE(o.created_at) = CURRENT_DATE
              AND o.status <> 'CANCELLED'
            """, nativeQuery = true)
    long customersToday();

    @Query(value = """
            SELECT DATE(o.created_at)                             AS sale_date,
                   COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total
            FROM orders o
            INNER JOIN order_items oi ON oi.id_order = o.id_order
            WHERE o.created_at >= CURRENT_DATE - INTERVAL '6 days'
              AND o.status <> 'CANCELLED'
            GROUP BY DATE(o.created_at)
            ORDER BY sale_date ASC
            """, nativeQuery = true)
    List<Object[]> salesByDay();

    @Query(value = """
            SELECT oi.product_name          AS name,
                   SUM(oi.quantity)         AS quantity
            FROM order_items oi
            INNER JOIN orders o ON o.id_order = oi.id_order
            WHERE o.created_at >= CURRENT_DATE - INTERVAL '29 days'
              AND o.status <> 'CANCELLED'
            GROUP BY oi.product_name
            ORDER BY quantity DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> topProducts();

    @Query(value = """
            SELECT
                o.payment_method                                  AS method,
                COUNT(o.id_order)                                 AS orders_count,
                COALESCE(SUM(oi.quantity * oi.unit_price), 0)    AS total
            FROM orders o
            INNER JOIN order_items oi ON oi.id_order = o.id_order
            WHERE DATE(o.created_at) BETWEEN :startDate AND :endDate
              AND o.status <> 'CANCELLED'
              AND o.payment_method IS NOT NULL
            GROUP BY o.payment_method
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> cashierReportByPayment(
            @org.springframework.data.repository.query.Param("startDate") String startDate,
            @org.springframework.data.repository.query.Param("endDate") String endDate);
}
