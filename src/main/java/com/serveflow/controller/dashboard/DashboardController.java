package com.serveflow.controller.dashboard;

import com.serveflow.repository.dashboard.DashboardReadRepository;
import com.serveflow.service.dashboard.GetDashboardMetricsUseCase;
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

    private final GetDashboardMetricsUseCase metricsUseCase;
    private final DashboardReadRepository    readRepository;

    public DashboardController(GetDashboardMetricsUseCase metricsUseCase,
                               DashboardReadRepository readRepository) {
        this.metricsUseCase = metricsUseCase;
        this.readRepository = readRepository;
    }

    @Operation(summary = "KPIs do dia com comparação vs ontem e vs mesmo dia semana passada")
    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsOutput> metrics() {
        GetDashboardMetricsUseCase.Output out = metricsUseCase.execute();
        return ResponseEntity.ok(new DashboardMetricsOutput(
                out.revenueToday(),     out.ordersToday(),     out.customersToday(),     out.ticketMedio(),
                out.revenueYesterday(), out.ordersYesterday(), out.customersYesterday(), out.ticketMedioYesterday(),
                out.revenueSameDayLastWeek(), out.ordersSameDayLastWeek(), out.customersSameDayLastWeek(), out.ticketMedioSameDayLastWeek(),
                out.openOrdersToday()
        ));
    }

    @Operation(summary = "Vendas por dia com período dinâmico")
    @GetMapping("/sales-by-day")
    public ResponseEntity<List<DailySales>> salesByDay(
            @RequestParam(defaultValue = "7") int days) {
        LocalDate startDate = LocalDate.now().minusDays((long) days * 2 - 1);
        List<DailySales> result = readRepository.salesByDay(startDate).stream()
                .map(row -> new DailySales(
                        LocalDate.parse(row[0].toString(), DateTimeFormatter.ISO_LOCAL_DATE),
                        row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO))
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Produtos mais vendidos — com filtro de período e receita")
    @GetMapping("/top-products")
    public ResponseEntity<List<TopProduct>> topProducts(
            @RequestParam(defaultValue = "30") int days) {

        LocalDate startDate = LocalDate.now().minusDays(Math.max(days - 1, 0));
        List<TopProduct> result = readRepository.topProductsByPeriod(startDate).stream()
                .map(row -> new TopProduct(
                        row[0] != null ? row[0].toString() : "—",
                        row[1] != null ? ((Number) row[1]).intValue() : 0,
                        row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO,
                        row[3] != null ? row[3].toString() : null))
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

        List<PaymentSummary> payments = readRepository.cashierReportByPayment(start, end).stream()
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
            int        ordersToday,
            int        customersToday,
            BigDecimal ticketMedio,
            BigDecimal revenueYesterday,
            int        ordersYesterday,
            int        customersYesterday,
            BigDecimal ticketMedioYesterday,
            BigDecimal revenueSameDayLastWeek,
            int        ordersSameDayLastWeek,
            int        customersSameDayLastWeek,
            BigDecimal ticketMedioSameDayLastWeek,
            int        openOrdersToday
    ) {}

    public record DailySales(LocalDate date, BigDecimal total) {}

    public record TopProduct(String name, int quantity, BigDecimal revenue, String imageUrl) {}

    public record PaymentSummary(String method, int ordersCount, BigDecimal total) {}

    public record CashierReport(
            LocalDate startDate,
            LocalDate endDate,
            List<PaymentSummary> byPaymentMethod,
            BigDecimal grossTotal
    ) {}
}
