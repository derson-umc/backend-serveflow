package com.serveflow.model.cashier;

import com.serveflow.exception.cashier.CashSessionAlreadyClosedException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CashSession {

    private final UUID id;
    private CashSessionStatus status;
    private final BigDecimal initialBalance;
    private final String observation;
    private final LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private final String openedBy;
    private String closedBy;
    private String closingObservation;
    private final Long version;

    public CashSession(UUID id, CashSessionStatus status, BigDecimal initialBalance,
                       String observation, LocalDateTime openedAt, LocalDateTime closedAt,
                       String openedBy, String closedBy, String closingObservation, Long version) {
        this.id = id;
        this.status = status;
        this.initialBalance = initialBalance;
        this.observation = observation;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.openedBy = openedBy;
        this.closedBy = closedBy;
        this.closingObservation = closingObservation;
        this.version = version;
    }

    public static CashSession open(BigDecimal initialBalance, String observation, String openedBy) {
        return new CashSession(UUID.randomUUID(), CashSessionStatus.OPEN,
                initialBalance != null ? initialBalance : BigDecimal.ZERO,
                observation, LocalDateTime.now(), null, openedBy, null, null, null);
    }

    public void close(String closedBy, String closingObservation) {
        if (status == CashSessionStatus.CLOSED) {
            throw new CashSessionAlreadyClosedException(id);
        }
        this.status = CashSessionStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.closedBy = closedBy;
        this.closingObservation = closingObservation;
    }
}
