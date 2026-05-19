package com.serveflow.model.financial;

import com.serveflow.exception.financial.DuplicateSettlementException;
import com.serveflow.exception.financial.InconsistentAmountException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AccountPayable {

    private final UUID id;
    private String description;
    private LocalDate dueDate;
    private BigDecimal amount;
    private AccountStatus status;
    private LocalDateTime paidAt;
    private BigDecimal paidAmount;
    private String category;
    private String supplier;
    private final LocalDateTime createdAt;
    private final Long version;

    public AccountPayable(UUID id, String description, LocalDate dueDate, BigDecimal amount,
                          AccountStatus status, LocalDateTime paidAt, BigDecimal paidAmount,
                          String category, String supplier, LocalDateTime createdAt, Long version) {
        this.id = id;
        this.description = description;
        this.dueDate = dueDate;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
        this.paidAmount = paidAmount;
        this.category = category;
        this.supplier = supplier;
        this.createdAt = createdAt;
        this.version = version;
    }

    public static AccountPayable create(String description, LocalDate dueDate, BigDecimal amount,
                                        String category, String supplier) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InconsistentAmountException("Payable amount must be greater than zero.");
        }
        return new AccountPayable(UUID.randomUUID(), description, dueDate, amount,
                AccountStatus.PENDING, null, null, category, supplier,
                LocalDateTime.now(), null);
    }

    public void registerPayment(BigDecimal settledAmount) {
        if (status == AccountStatus.PAID || status == AccountStatus.CANCELLED) {
            throw new DuplicateSettlementException(id);
        }
        if (settledAmount == null || settledAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InconsistentAmountException("Paid amount must be greater than zero.");
        }
        this.paidAmount = settledAmount;
        this.paidAt = LocalDateTime.now();
        this.status = AccountStatus.PAID;
    }

    public void cancel() {
        if (status == AccountStatus.PAID) {
            throw new InconsistentAmountException("A paid account cannot be cancelled without a reopen justification.");
        }
        this.status = AccountStatus.CANCELLED;
    }

    public void markOverdue() {
        if (status == AccountStatus.PENDING && LocalDate.now().isAfter(dueDate)) {
            this.status = AccountStatus.OVERDUE;
        }
    }
}
