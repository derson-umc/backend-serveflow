package com.serveflow.repository.cashier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringCashMovementRepository extends JpaRepository<CashMovementEntity, UUID> {

    List<CashMovementEntity> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

}


