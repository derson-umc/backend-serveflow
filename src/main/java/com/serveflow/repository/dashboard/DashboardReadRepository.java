package com.serveflow.repository.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DashboardReadRepository {

    BigDecimal revenueToday();
    BigDecimal revenueYesterday();

    long ordersToday();
    long ordersYesterday();
    long customersToday();
    long customersYesterday();
    List<Object[]> salesByDay();
    List<Object[]> topProductsByPeriod(LocalDate startDate);
    List<Object[]> cashierReportByPayment(LocalDate startDate, LocalDate endDate);

}
