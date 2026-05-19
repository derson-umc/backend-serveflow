package com.serveflow.repository.cashier;

import com.serveflow.model.cashier.CashSessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashSessionEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CashSessionStatus status;

    @Column(name = "initial_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal initialBalance;

    @Column(name = "observation", length = 500)
    private String observation;

    @Column(name = "opened_at", updatable = false, nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "opened_by", nullable = false, length = 150)
    private String openedBy;

    @Column(name = "closed_by", length = 150)
    private String closedBy;

    @Column(name = "closing_observation", length = 500)
    private String closingObservation;

    @Override
    public boolean isNew() { return version == null; }

    @PrePersist
    protected void onCreate() {
        if (openedAt == null) openedAt = LocalDateTime.now();
        if (status == null) status = CashSessionStatus.OPEN;
        if (initialBalance == null) initialBalance = BigDecimal.ZERO;
    }
}
