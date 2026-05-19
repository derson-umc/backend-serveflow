package com.serveflow.repository.stock.StockMovement;

import java.math.BigDecimal;

public interface StockConsolidatedRow {
    String getInsumo();
    String getUnidade();
    BigDecimal getTotalEntradas();
    BigDecimal getTotalSaidas();
    BigDecimal getSaldoAtual();
}
