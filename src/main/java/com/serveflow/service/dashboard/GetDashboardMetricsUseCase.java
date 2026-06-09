package com.serveflow.service.dashboard;

import com.serveflow.repository.dashboard.DashboardReadRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class GetDashboardMetricsUseCase {

    private final DashboardReadRepository repo;

    public GetDashboardMetricsUseCase(DashboardReadRepository repo) {
        this.repo = repo;
    }

    public Output execute() {
        BigDecimal revenueToday   = repo.revenueToday();
        long       ordersToday    = repo.ordersToday();
        long       customersToday = repo.customersToday();

        BigDecimal ticketMedio = customersToday > 0
                ? revenueToday.divide(BigDecimal.valueOf(customersToday), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal revenueYesterday   = repo.revenueYesterday();
        long       ordersYesterday    = repo.ordersYesterday();
        long       customersYesterday = repo.customersYesterday();

        BigDecimal ticketMedioYesterday = customersYesterday > 0
                ? revenueYesterday.divide(BigDecimal.valueOf(customersYesterday), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new Output(
                revenueToday,     (int) ordersToday,    (int) customersToday,    ticketMedio,
                revenueYesterday, (int) ordersYesterday, (int) customersYesterday, ticketMedioYesterday
        );
    }

    public record Output(
            BigDecimal revenueToday,
            int        ordersToday,
            int        customersToday,
            BigDecimal ticketMedio,
            BigDecimal revenueYesterday,
            int        ordersYesterday,
            int        customersYesterday,
            BigDecimal ticketMedioYesterday
    ) {}
}
