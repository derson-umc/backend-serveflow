package com.serveflow.repository.dashboard;

import com.serveflow.repository.order.SpringOrderRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class JpaDashboardReadRepository implements DashboardReadRepository {

    private final SpringOrderRepository orderRepo;

    public JpaDashboardReadRepository(SpringOrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    public BigDecimal revenueToday() {
        return safe(orderRepo.revenueToday());
    }

    @Override
    public BigDecimal revenueYesterday() {
        return safe(orderRepo.revenueYesterday());
    }

    @Override
    public long ordersToday() {
        return orderRepo.ordersToday();
    }

    @Override
    public long ordersYesterday() {
        return orderRepo.ordersYesterday();
    }

    @Override
    public long customersToday() {
        return orderRepo.customersToday();
    }

    @Override
    public long customersYesterday() {
        return orderRepo.customersYesterday();
    }

    @Override
    public List<Object[]> salesByDay() {
        return orderRepo.salesByDay();
    }

    @Override
    public List<Object[]> topProductsByPeriod(LocalDate startDate) {
        return orderRepo.topProductsByPeriod(startDate);
    }

    @Override
    public List<Object[]> cashierReportByPayment(LocalDate startDate, LocalDate endDate) {
        return orderRepo.cashierReportByPayment(startDate, endDate);
    }

    private static BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
