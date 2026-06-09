package com.serveflow.repository.audit.log;

import com.serveflow.repository.audit.ErrorLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SpringErrorLogRepository extends JpaRepository<ErrorLogEntity, Long> {

    @Modifying
    @Query("DELETE FROM ErrorLogEntity e WHERE e.createdAt < :threshold")
    int deleteByCreatedAtBefore(@Param("threshold") LocalDateTime threshold);
}
