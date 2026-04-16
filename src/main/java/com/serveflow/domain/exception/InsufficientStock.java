package com.serveflow.domain.exception;

import java.math.BigDecimal;

public class InsufficientStock extends RuntimeException {
    public InsufficientStock(String itemName, BigDecimal available, BigDecimal required) {
        super("Estoque insuficiente para '" + itemName
                + "'. Disponível: " + available
                + ", Requerido: " + required + ".");
    }
}
