package com.serveflow.repository.cashier;

import com.serveflow.model.cashier.CashSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringCashSessionRepository extends JpaRepository<CashSessionEntity, UUID> {

    Optional<CashSessionEntity> findFirstByStatusOrderByOpenedAtDesc(CashSessionStatus status);

    boolean existsByStatus(CashSessionStatus status);

    List<CashSessionEntity> findAllByOrderByOpenedAtDesc();
}
