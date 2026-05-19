package com.serveflow.repository.financial;

import com.serveflow.model.financial.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts_receivable")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountReceivableEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "description", nullable = false, length = 300)
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "received_amount", precision = 12, scale = 2)
    private BigDecimal receivedAmount;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "source_order_id")
    private UUID sourceOrderId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean isNew() { return version == null; }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = AccountStatus.PENDING;
    }
}
