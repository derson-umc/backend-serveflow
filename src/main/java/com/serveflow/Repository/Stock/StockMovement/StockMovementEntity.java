package com.serveflow.Repository.Stock.StockMovement;

import com.serveflow.Model.Stock.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementEntity {

    @Id
    @Column(name = "id_movement", updatable = false, nullable = false)
    private UUID idMovement;

    @Column(name = "stock_item_id", nullable = false)
    private UUID stockItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MovementType type;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(length = 500)
    private String reason;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
