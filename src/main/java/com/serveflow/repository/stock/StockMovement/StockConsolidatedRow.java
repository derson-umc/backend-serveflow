package com.serveflow.repository.stock.stockmovement;

import java.math.BigDecimal;

public interface StockConsolidatedRow {
    String getInsumo();
    String getUnidade();
    BigDecimal getTotalEntradas();
    BigDecimal getTotalSaidas();
    BigDecimal getSaldoAtual();
}
