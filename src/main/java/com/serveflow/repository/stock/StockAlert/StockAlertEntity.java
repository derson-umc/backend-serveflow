package com.serveflow.repository.stock.stockalert;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_alerts")
@Getter
@Setter
@NoArgsConstructor
public class StockAlertEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_alert", updatable = false, nullable = false)
    private UUID idAlert;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idAlert; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }

    @Column(name = "stock_item_id", nullable = false)
    private UUID stockItemId;

    @Column(name = "stock_item_name", nullable = false, length = 150)
    private String stockItemName;

    @Column(name = "current_qty", nullable = false, precision = 10, scale = 3)
    private BigDecimal currentQty;

    @Column(name = "minimum_qty", nullable = false, precision = 10, scale = 3)
    private BigDecimal minimumQty;

    @Column(nullable = false)
    private boolean resolved;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
