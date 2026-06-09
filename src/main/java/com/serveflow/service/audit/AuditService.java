package com.serveflow.service.audit;

import com.serveflow.repository.audit.AccessLogEntity;
import com.serveflow.repository.audit.AuditLogEntity;
import com.serveflow.repository.audit.ErrorLogEntity;
import com.serveflow.repository.audit.log.SpringAccessLogRepository;
import com.serveflow.repository.audit.log.SpringAuditLogRepository;
import com.serveflow.repository.audit.log.SpringErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final SpringAccessLogRepository accessLogRepo;
    private final SpringAuditLogRepository  auditLogRepo;
    private final SpringErrorLogRepository  errorLogRepo;

    @Async("auditExecutor")
    public void logAccess(Long userId, String ip, String endpoint,
                          String httpMethod, int httpStatus) {
        try {
            accessLogRepo.save(AccessLogEntity.of(userId, ip, endpoint, httpMethod, httpStatus));
        } catch (Exception e) {
            log.warn("Falha ao gravar access_log: {}", e.getMessage());
        }
    }

    @Async("auditExecutor")
    public void logLoginSuccess(Long userId, String username, String ip) {
        try {
            auditLogRepo.save(AuditLogEntity.of(userId, "LOGIN_SUCCESS", "User", userId, ip)
                    .withData(null, username));
            log.info("LOGIN_SUCCESS user={} ip={}", username, ip);
        } catch (Exception e) {
            log.warn("Falha ao gravar audit_log LOGIN_SUCCESS: {}", e.getMessage());
        }
    }

    @Async("auditExecutor")
    public void logLoginFailure(String username, String ip, String reason) {
        try {
            auditLogRepo.save(AuditLogEntity.of(null, "LOGIN_FAILURE", "User", null, ip)
                    .withData(null, username + " — " + reason));
            log.warn("LOGIN_FAILURE username={} ip={} reason={}", username, ip, reason);
        } catch (Exception e) {
            log.warn("Falha ao gravar audit_log LOGIN_FAILURE: {}", e.getMessage());
        }
    }

    @Async("auditExecutor")
    public void logLogout(Long userId, String username, String ip) {
        try {
            auditLogRepo.save(AuditLogEntity.of(userId, "LOGOUT", "User", userId, ip));
            log.info("LOGOUT user={} ip={}", username, ip);
        } catch (Exception e) {
            log.warn("Falha ao gravar audit_log LOGOUT: {}", e.getMessage());
        }
    }

    @Async("auditExecutor")
    public void logPasswordReset(String username, String ip, boolean success) {
        try {
            String action = success ? "PASSWORD_RESET_SUCCESS" : "PASSWORD_RESET_FAILURE";
            auditLogRepo.save(AuditLogEntity.of(null, action, "User", null, ip)
                    .withData(null, username));
        } catch (Exception e) {
            log.warn("Falha ao gravar audit_log PASSWORD_RESET: {}", e.getMessage());
        }
    }

    @Async("auditExecutor")
    public void logAction(Long userId, String action, String entity,
                          Long entityId, String ip) {
        try {
            auditLogRepo.save(AuditLogEntity.of(userId, action, entity, entityId, ip));
        } catch (Exception e) {
            log.warn("Falha ao gravar audit_log {}: {}", action, e.getMessage());
        }
    }

    private static final int MAX_STACKTRACE_LINES = 20;

    @Async("auditExecutor")
    public void logError(Throwable throwable, String service) {
        try {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            String truncated = sw.toString().lines()
                    .limit(MAX_STACKTRACE_LINES)
                    .reduce("", (a, b) -> a + b + "\n");
            errorLogRepo.save(ErrorLogEntity.of(throwable.getMessage(), truncated, service));
        } catch (Exception e) {
            log.warn("Falha ao gravar error_log: {}", e.getMessage());
        }
    }
}
