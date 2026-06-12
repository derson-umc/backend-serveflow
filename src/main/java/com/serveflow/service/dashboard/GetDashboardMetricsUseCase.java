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

        BigDecimal ticketMedio = ordersToday > 0
                ? revenueToday.divide(BigDecimal.valueOf(ordersToday), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal revenueYesterday   = repo.revenueYesterday();
        long       ordersYesterday    = repo.ordersYesterday();
        long       customersYesterday = repo.customersYesterday();

        BigDecimal ticketMedioYesterday = ordersYesterday > 0
                ? revenueYesterday.divide(BigDecimal.valueOf(ordersYesterday), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal revenueSameDayLastWeek   = repo.revenueSameDayLastWeek();
        long       ordersSameDayLastWeek    = repo.ordersSameDayLastWeek();
        long       customersSameDayLastWeek = repo.customersSameDayLastWeek();

        BigDecimal ticketMedioSameDayLastWeek = ordersSameDayLastWeek > 0
                ? revenueSameDayLastWeek.divide(BigDecimal.valueOf(ordersSameDayLastWeek), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long openOrdersToday = repo.openOrdersToday();

        return new Output(
                revenueToday,     (int) ordersToday,    (int) customersToday,    ticketMedio,
                revenueYesterday, (int) ordersYesterday, (int) customersYesterday, ticketMedioYesterday,
                revenueSameDayLastWeek, (int) ordersSameDayLastWeek, (int) customersSameDayLastWeek, ticketMedioSameDayLastWeek,
                (int) openOrdersToday
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
            BigDecimal ticketMedioYesterday,
            BigDecimal revenueSameDayLastWeek,
            int        ordersSameDayLastWeek,
            int        customersSameDayLastWeek,
            BigDecimal ticketMedioSameDayLastWeek,
            int        openOrdersToday
    ) {}
}
