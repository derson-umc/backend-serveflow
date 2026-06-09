package com.serveflow.service.audit;

import com.serveflow.repository.audit.SpringAccessLogRepository;
import com.serveflow.repository.audit.SpringAuditLogRepository;
import com.serveflow.repository.audit.SpringErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogCleanupJob {

    private final SpringAccessLogRepository accessLogRepo;
    private final SpringAuditLogRepository  auditLogRepo;
    private final SpringErrorLogRepository  errorLogRepo;

    @Value("${app.audit.retention-days:90}")
    private int retentionDays;

    @Scheduled(cron = "0 0 2 */2 * *")
    @Transactional
    public void purgeOldLogs() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);

        int access = accessLogRepo.deleteByCreatedAtBefore(threshold);
        int audit  = auditLogRepo.deleteByCreatedAtBefore(threshold);
        int error  = errorLogRepo.deleteByCreatedAtBefore(threshold);

        log.info("AuditLogCleanupJob: removidos access={} audit={} error={} (threshold={})",
                access, audit, error, threshold.toLocalDate());
    }
}
