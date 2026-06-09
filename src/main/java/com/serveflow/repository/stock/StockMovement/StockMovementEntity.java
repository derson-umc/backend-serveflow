package com.serveflow.repository.stock.stockmovement;

import com.serveflow.model.stock.MovementType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_movement", updatable = false, nullable = false)
    private UUID idMovement;

    @Column(name = "stock_item_id", nullable = false)
    private UUID stockItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType type;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(name = "stock_item_name", length = 150)
    private String stockItemName;

    @Column(name = "balance_before", precision = 12, scale = 4)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 12, scale = 4)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String reason;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idMovement; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
