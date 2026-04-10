package com.serveflow.domain.exception;

import java.math.BigDecimal;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String itemName, BigDecimal available, BigDecimal required) {
        super("Estoque insuficiente para '" + itemName
                + "'. Disponivel: " + available
                + ", Requerido: " + required + ".");
    }
}
