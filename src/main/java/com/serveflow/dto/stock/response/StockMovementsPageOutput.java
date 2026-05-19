package com.serveflow.dto.stock.response;

import java.util.List;

public record StockMovementsPageOutput(
        List<StockMovementOutput> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
