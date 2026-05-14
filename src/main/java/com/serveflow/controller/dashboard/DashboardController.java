package com.serveflow.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Tag(name = "Dashboard", description = "Métricas analíticas do gestor")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Operation(summary = "KPIs principais do dia")
    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsOutput> metrics() {
        return ResponseEntity.ok(new DashboardMetricsOutput(
                new BigDecimal("12450.80"),
                187,
                124,
                new BigDecimal("3120.45")
        ));
    }

    @Operation(summary = "Vendas por dia (últimos 7 dias)")
    @GetMapping("/sales-by-day")
    public ResponseEntity<List<DailySales>> salesByDay() {
        // Stub: substituir por SUM(total) GROUP BY DATE(...)
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(List.of(
                new DailySales(today.minusDays(6), bd(8200, 14500)),
                new DailySales(today.minusDays(5), bd(8200, 14500)),
                new DailySales(today.minusDays(4), bd(8200, 14500)),
                new DailySales(today.minusDays(3), bd(8200, 14500)),
                new DailySales(today.minusDays(2), bd(8200, 14500)),
                new DailySales(today.minusDays(1), bd(8200, 14500)),
                new DailySales(today, bd(8200, 14500))
        ));
    }

    @Operation(summary = "Produtos mais vendidos")
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProduct>> topProducts() {
        return ResponseEntity.ok(List.of(
                new TopProduct("Pizza Margherita", 84),
                new TopProduct("Hambúrguer Artesanal", 71),
                new TopProduct("Lasanha Bolonhesa", 53),
                new TopProduct("Sushi Combo", 47),
                new TopProduct("Petit Gateau", 38)
        ));
    }

    private BigDecimal bd(int min, int max) {
        return BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(min, max));
    }

    public record DashboardMetricsOutput(
            BigDecimal revenueToday,
            int ordersToday,
            int customersToday,
            BigDecimal netProfit
    ) {}

    public record DailySales(LocalDate date, BigDecimal total) {}

    public record TopProduct(String name, int quantity) {}
}
