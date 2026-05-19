package com.serveflow.controller.dashboard;

import com.serveflow.repository.order.SpringOrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Tag(name = "Dashboard", description = "Métricas analíticas do gestor")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final SpringOrderRepository orderRepository;

    public DashboardController(SpringOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Operation(summary = "KPIs principais do dia")
    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsOutput> metrics() {
        BigDecimal revenue = orderRepository.revenueToday();
        if (revenue == null) revenue = BigDecimal.ZERO;

        long orders    = orderRepository.ordersToday();
        long customers = orderRepository.customersToday();

        BigDecimal profit = revenue.multiply(new BigDecimal("0.30"));

        return ResponseEntity.ok(new DashboardMetricsOutput(revenue, (int) orders, (int) customers, profit));
    }

    @Operation(summary = "Vendas por dia (últimos 7 dias)")
    @GetMapping("/sales-by-day")
    public ResponseEntity<List<DailySales>> salesByDay() {
        List<Object[]> rows = orderRepository.salesByDay();
        List<DailySales> result = rows.stream()
                .map(row -> new DailySales(
                        LocalDate.parse(row[0].toString(), DateTimeFormatter.ISO_LOCAL_DATE),
                        row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO))
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Produtos mais vendidos (últimos 30 dias)")
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProduct>> topProducts() {
        List<Object[]> rows = orderRepository.topProducts();
        List<TopProduct> result = rows.stream()
                .map(row -> new TopProduct(
                        row[0] != null ? row[0].toString() : "—",
                        row[1] != null ? ((Number) row[1]).intValue() : 0))
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Relatório de faturamento do caixa por período")
    @GetMapping("/cashier-report")
    public ResponseEntity<CashierReport> cashierReport(
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate) {

        LocalDate start = startDate.isBlank() ? LocalDate.now() : LocalDate.parse(startDate);
        LocalDate end   = endDate.isBlank()   ? LocalDate.now() : LocalDate.parse(endDate);

        List<Object[]> rows = orderRepository.cashierReportByPayment(
                start.toString(), end.toString());

        List<PaymentSummary> payments = rows.stream()
                .map(row -> new PaymentSummary(
                        row[0] != null ? row[0].toString() : "SEM_PAGAMENTO",
                        row[1] != null ? ((Number) row[1]).intValue() : 0,
                        row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO))
                .toList();

        BigDecimal grossTotal = payments.stream()
                .map(PaymentSummary::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(new CashierReport(start, end, payments, grossTotal));
    }

    public record DashboardMetricsOutput(
            BigDecimal revenueToday,
            int ordersToday,
            int customersToday,
            BigDecimal netProfit
    ) {}

    public record DailySales(LocalDate date, BigDecimal total) {}

    public record TopProduct(String name, int quantity) {}

    public record PaymentSummary(String method, int ordersCount, BigDecimal total) {}

    public record CashierReport(
            LocalDate startDate,
            LocalDate endDate,
            List<PaymentSummary> byPaymentMethod,
            BigDecimal grossTotal
    ) {}
}
