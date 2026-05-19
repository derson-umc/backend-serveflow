package com.serveflow.repository.financial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringFinancialAuditRepository extends JpaRepository<FinancialAuditEntity, UUID> {

    List<FinancialAuditEntity> findByEntityIdOrderByCreatedAtDesc(UUID entityId);

    List<FinancialAuditEntity> findAllByOrderByCreatedAtDesc();
}
