package com.serveflow.model.financial;

import com.serveflow.exception.financial.DuplicateSettlementException;
import com.serveflow.exception.financial.InconsistentAmountException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AccountReceivable {

    private final UUID id;
    private String description;
    private LocalDate dueDate;
    private BigDecimal amount;
    private AccountStatus status;
    private LocalDateTime receivedAt;
    private BigDecimal receivedAmount;
    private String category;
    private UUID sourceOrderId;
    private final LocalDateTime createdAt;
    private final Long version;

    public AccountReceivable(UUID id, String description, LocalDate dueDate, BigDecimal amount,
                             AccountStatus status, LocalDateTime receivedAt, BigDecimal receivedAmount,
                             String category, UUID sourceOrderId, LocalDateTime createdAt, Long version) {
        this.id = id;
        this.description = description;
        this.dueDate = dueDate;
        this.amount = amount;
        this.status = status;
        this.receivedAt = receivedAt;
        this.receivedAmount = receivedAmount;
        this.category = category;
        this.sourceOrderId = sourceOrderId;
        this.createdAt = createdAt;
        this.version = version;
    }

    public static AccountReceivable create(String description, LocalDate dueDate, BigDecimal amount,
                                           String category, UUID sourceOrderId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InconsistentAmountException("Receivable amount must be greater than zero.");
        }
        return new AccountReceivable(UUID.randomUUID(), description, dueDate, amount,
                AccountStatus.PENDING, null, null, category, sourceOrderId,
                LocalDateTime.now(), null);
    }

    public void registerReceipt(BigDecimal settledAmount) {
        if (status == AccountStatus.RECEIVED || status == AccountStatus.CANCELLED) {
            throw new DuplicateSettlementException(id);
        }
        if (settledAmount == null || settledAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InconsistentAmountException("Received amount must be greater than zero.");
        }
        this.receivedAmount = settledAmount;
        this.receivedAt = LocalDateTime.now();
        this.status = AccountStatus.RECEIVED;
    }

    public void cancel() {
        if (status == AccountStatus.RECEIVED) {
            throw new InconsistentAmountException("A received account cannot be cancelled without a reopen justification.");
        }
        this.status = AccountStatus.CANCELLED;
    }

    public void markOverdue() {
        if (status == AccountStatus.PENDING && LocalDate.now().isAfter(dueDate)) {
            this.status = AccountStatus.OVERDUE;
        }
    }
}
