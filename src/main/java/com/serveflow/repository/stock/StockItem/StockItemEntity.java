package com.serveflow.repository.stock.StockItem;

import com.serveflow.model.stock.StockItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockItemEntity implements Persistable<UUID> {

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

    @Column(length = 100)
    private String category;

    @Column(length = 200)
    private String supplier;

    @Column(name = "average_cost", precision = 10, scale = 4)
    private BigDecimal averageCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockItemStatus status = StockItemStatus.ACTIVE;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idStockItem; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = StockItemStatus.ACTIVE;
    }
}
