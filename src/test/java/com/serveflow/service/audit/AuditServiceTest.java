package com.serveflow.service.audit;

import com.serveflow.repository.audit.AccessLogEntity;
import com.serveflow.repository.audit.AuditLogEntity;
import com.serveflow.repository.audit.ErrorLogEntity;
import com.serveflow.repository.audit.log.SpringAccessLogRepository;
import com.serveflow.repository.audit.log.SpringAuditLogRepository;
import com.serveflow.repository.audit.log.SpringErrorLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    SpringAccessLogRepository accessLogRepo;
    @Mock
    SpringAuditLogRepository auditLogRepo;
    @Mock
    SpringErrorLogRepository errorLogRepo;

    @InjectMocks
    AuditService service;

    @Nested
    @DisplayName("logAccess()")
    class LogAccess {

        @Test
        @DisplayName("salva AccessLogEntity quando chamado")
        void logAccess_savesEntity() {
            service.logAccess(1L, "127.0.0.1", "/orders", "POST", 201);
            verify(accessLogRepo, timeout(2000)).save(any(AccessLogEntity.class));
        }

        @Test
        @DisplayName("continua sem lançar exceção quando repositório falha")
        void logAccess_silentFailure() {
            when(accessLogRepo.save(any())).thenThrow(new RuntimeException("DB error"));
            // Should not throw
            service.logAccess(1L, "127.0.0.1", "/orders", "POST", 201);
        }
    }

    @Nested
    @DisplayName("logLoginSuccess()")
    class LogLoginSuccess {

        @Test
        @DisplayName("salva AuditLogEntity quando chamado")
        void logLoginSuccess_savesEntity() {
            service.logLoginSuccess(1L, "admin", "127.0.0.1");
            verify(auditLogRepo, timeout(2000)).save(any(AuditLogEntity.class));
        }

        @Test
        @DisplayName("continua sem lançar exceção quando repositório falha")
        void logLoginSuccess_silentFailure() {
            when(auditLogRepo.save(any())).thenThrow(new RuntimeException("DB error"));
            service.logLoginSuccess(1L, "admin", "127.0.0.1");
        }
    }

    @Nested
    @DisplayName("logLoginFailure()")
    class LogLoginFailure {

        @Test
        @DisplayName("salva AuditLogEntity quando chamado")
        void logLoginFailure_savesEntity() {
            service.logLoginFailure("admin", "127.0.0.1", "bad credentials");
            verify(auditLogRepo, timeout(2000)).save(any(AuditLogEntity.class));
        }

        @Test
        @DisplayName("continua sem lançar exceção quando repositório falha")
        void logLoginFailure_silentFailure() {
            when(auditLogRepo.save(any())).thenThrow(new RuntimeException("DB error"));
            service.logLoginFailure("admin", "127.0.0.1", "bad credentials");
        }
    }

    @Nested
    @DisplayName("logLogout()")
    class LogLogout {

        @Test
        @DisplayName("salva AuditLogEntity quando chamado")
        void logLogout_savesEntity() {
            service.logLogout(1L, "admin", "127.0.0.1");
            verify(auditLogRepo, timeout(2000)).save(any(AuditLogEntity.class));
        }

        @Test
        @DisplayName("continua sem lançar exceção quando repositório falha")
        void logLogout_silentFailure() {
            when(auditLogRepo.save(any())).thenThrow(new RuntimeException("DB error"));
            service.logLogout(1L, "admin", "127.0.0.1");
        }
    }

    @Nested
    @DisplayName("logPasswordReset()")
    class LogPasswordReset {

        @Test
        @DisplayName("salva AuditLogEntity para sucesso")
        void logPasswordReset_success_savesEntity() {
            service.logPasswordReset("admin", "127.0.0.1", true);
            verify(auditLogRepo, timeout(2000)).save(any(AuditLogEntity.class));
        }

        @Test
        @DisplayName("salva AuditLogEntity para falha")
        void logPasswordReset_failure_savesEntity() {
            service.logPasswordReset("admin", "127.0.0.1", false);
            verify(auditLogRepo, timeout(2000)).save(any(AuditLogEntity.class));
        }

        @Test
        @DisplayName("continua sem lançar exceção quando repositório falha")
        void logPasswordReset_silentFailure() {
            when(auditLogRepo.save(any())).thenThrow(new RuntimeException("DB error"));
            service.logPasswordReset("admin", "127.0.0.1", true);
        }
    }

    @Nested
    @DisplayName("logAction()")
    class LogAction {

        @Test
        @DisplayName("salva AuditLogEntity quando chamado")
        void logAction_savesEntity() {
            service.logAction(1L, "ORDER_CREATE", "Order", 123L, "127.0.0.1");
            verify(auditLogRepo, timeout(2000)).save(any(AuditLogEntity.class));
        }

        @Test
        @DisplayName("continua sem lançar exceção quando repositório falha")
        void logAction_silentFailure() {
            when(auditLogRepo.save(any())).thenThrow(new RuntimeException("DB error"));
            service.logAction(1L, "ORDER_CREATE", "Order", null, "127.0.0.1");
        }
    }

    @Nested
    @DisplayName("logError()")
    class LogError {

        @Test
        @DisplayName("salva ErrorLogEntity quando chamado")
        void logError_savesEntity() {
            RuntimeException ex = new RuntimeException("Something broke");
            service.logError(ex, "OrderService");
            verify(errorLogRepo, timeout(2000)).save(any(ErrorLogEntity.class));
        }

        @Test
        @DisplayName("continua sem lançar exceção quando repositório falha")
        void logError_silentFailure() {
            when(errorLogRepo.save(any())).thenThrow(new RuntimeException("DB error"));
            RuntimeException ex = new RuntimeException("test error");
            service.logError(ex, "SomeService");
        }

        @Test
        @DisplayName("trata exceção com stacktrace longa sem problema")
        void logError_longStacktrace() {
            // Create an exception with many nested causes
            RuntimeException nested = new RuntimeException("nested");
            for (int i = 0; i < 30; i++) {
                nested = new RuntimeException("level " + i, nested);
            }
            service.logError(nested, "SomeService");
            verify(errorLogRepo, timeout(2000)).save(any(ErrorLogEntity.class));
        }
    }
}
