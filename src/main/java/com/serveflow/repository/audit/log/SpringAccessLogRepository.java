package com.serveflow.repository.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SpringAccessLogRepository extends JpaRepository<AccessLogEntity, Long> {

    @Modifying
    @Query("DELETE FROM AccessLogEntity a WHERE a.createdAt < :threshold")
    int deleteByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);
}
