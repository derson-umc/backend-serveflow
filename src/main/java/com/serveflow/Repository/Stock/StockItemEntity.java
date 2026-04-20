package com.serveflow.Repository.Stock;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockItemEntity {

    @Id
    @Column(name = "id_stock_item", updatable = false, nullable = false)
    private UUID idStockItem;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "current_quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal currentQuantity;

    @Column(name = "minimum_quantity", nullable = false, precision = 12, scale = 4)
    private BigDecimal minimumQuantity;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
