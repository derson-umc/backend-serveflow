package com.serveflow.service.financial;

import com.serveflow.dto.financial.response.FinancialAuditOutput;
import com.serveflow.model.financial.AccountStatus;
import com.serveflow.repository.financial.AccountPayableEntity;
import com.serveflow.repository.financial.AccountReceivableEntity;
import com.serveflow.repository.financial.FinancialAuditEntity;
import com.serveflow.repository.financial.SpringAccountPayableRepository;
import com.serveflow.repository.financial.SpringAccountReceivableRepository;
import com.serveflow.repository.financial.SpringFinancialAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FinancialServiceExtendedTest {

    @Mock SpringAccountReceivableRepository receivableRepository;
    @Mock SpringAccountPayableRepository payableRepository;
    @Mock SpringFinancialAuditRepository auditRepository;

    @InjectMocks FinancialService service;

    @BeforeEach
    void setUp() {
        when(auditRepository.save(any(FinancialAuditEntity.class))).thenReturn(new FinancialAuditEntity());
    }

    private AccountReceivableEntity overdueReceivable(UUID id) {
        AccountReceivableEntity e = new AccountReceivableEntity();
        e.setId(id);
        e.setDescription("Venda A");
        e.setDueDate(LocalDate.now().minusDays(5));
        e.setAmount(new BigDecimal("100.00"));
        e.setStatus(AccountStatus.PENDING);
        e.setCreatedAt(LocalDateTime.now().minusDays(10));
        return e;
    }

    private AccountPayableEntity overduePayable(UUID id) {
        AccountPayableEntity e = new AccountPayableEntity();
        e.setId(id);
        e.setDescription("Fornecedor B");
        e.setDueDate(LocalDate.now().minusDays(3));
        e.setAmount(new BigDecimal("200.00"));
        e.setStatus(AccountStatus.PENDING);
        e.setCreatedAt(LocalDateTime.now().minusDays(8));
        return e;
    }

    private FinancialAuditEntity auditEntity(UUID entityId, String action) {
        FinancialAuditEntity e = new FinancialAuditEntity();
        e.setId(UUID.randomUUID());
        e.setEntityType("ACCOUNTS_RECEIVABLE");
        e.setEntityId(entityId);
        e.setAction(action);
        e.setPerformedBy("system");
        e.setDescription("Test audit entry");
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    @Nested
    @DisplayName("processOverdueAccounts()")
    class ProcessOverdue {

        @Test
        @DisplayName("marca recebíveis vencidos como OVERDUE")
        void processOverdue_marksReceivablesAsOverdue() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            AccountReceivableEntity rec1 = overdueReceivable(id1);
            AccountReceivableEntity rec2 = overdueReceivable(id2);
            when(receivableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), eq(AccountStatus.PENDING)))
                    .thenReturn(List.of(rec1, rec2));
            when(payableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), eq(AccountStatus.PENDING)))
                    .thenReturn(List.of());
            when(receivableRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.processOverdueAccounts();

            assertThat(rec1.getStatus()).isEqualTo(AccountStatus.OVERDUE);
            assertThat(rec2.getStatus()).isEqualTo(AccountStatus.OVERDUE);
            verify(receivableRepository, times(2)).save(any(AccountReceivableEntity.class));
        }

        @Test
        @DisplayName("marca pagáveis vencidos como OVERDUE")
        void processOverdue_marksPayablesAsOverdue() {
            UUID id = UUID.randomUUID();
            AccountPayableEntity pay = overduePayable(id);
            when(receivableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), eq(AccountStatus.PENDING)))
                    .thenReturn(List.of());
            when(payableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), eq(AccountStatus.PENDING)))
                    .thenReturn(List.of(pay));
            when(payableRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.processOverdueAccounts();

            assertThat(pay.getStatus()).isEqualTo(AccountStatus.OVERDUE);
            verify(payableRepository).save(any(AccountPayableEntity.class));
        }

        @Test
        @DisplayName("nada é feito quando não há contas vencidas")
        void processOverdue_doesNothing_whenNoOverdue() {
            when(receivableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), eq(AccountStatus.PENDING)))
                    .thenReturn(List.of());
            when(payableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), eq(AccountStatus.PENDING)))
                    .thenReturn(List.of());

            service.processOverdueAccounts();

            verify(receivableRepository, never()).save(any());
            verify(payableRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listAudit()")
    class ListAudit {

        @Test
        @DisplayName("retorna lista de auditorias ordenada por data decrescente")
        void listAudit_returnsAllAudits() {
            UUID entityId = UUID.randomUUID();
            FinancialAuditEntity e1 = auditEntity(entityId, "CREATE");
            FinancialAuditEntity e2 = auditEntity(entityId, "SETTLE");
            when(auditRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(e2, e1));

            List<FinancialAuditOutput> result = service.listAudit();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).action()).isEqualTo("SETTLE");
            assertThat(result.get(1).action()).isEqualTo("CREATE");
        }

        @Test
        @DisplayName("retorna lista vazia quando não há auditorias")
        void listAudit_returnsEmptyList() {
            when(auditRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

            List<FinancialAuditOutput> result = service.listAudit();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listAuditByEntity()")
    class ListAuditByEntity {

        @Test
        @DisplayName("retorna auditorias filtradas por entityId")
        void listAuditByEntity_returnsFilteredAudits() {
            UUID entityId = UUID.randomUUID();
            FinancialAuditEntity e = auditEntity(entityId, "CANCEL");
            when(auditRepository.findByEntityIdOrderByCreatedAtDesc(entityId))
                    .thenReturn(List.of(e));

            List<FinancialAuditOutput> result = service.listAuditByEntity(entityId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).entityId()).isEqualTo(entityId);
            assertThat(result.get(0).action()).isEqualTo("CANCEL");
        }

        @Test
        @DisplayName("retorna lista vazia quando entityId sem auditorias")
        void listAuditByEntity_returnsEmpty_whenNoAudits() {
            UUID entityId = UUID.randomUUID();
            when(auditRepository.findByEntityIdOrderByCreatedAtDesc(entityId))
                    .thenReturn(List.of());

            List<FinancialAuditOutput> result = service.listAuditByEntity(entityId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("calculateCashFlow()")
    class CashFlow {

        @Test
        @DisplayName("calcula fluxo de caixa com listas vazias retorna zeros")
        void cashFlow_allEmpty_returnsZeros() {
            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.RECEIVED))
                    .thenReturn(List.of());
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PAID))
                    .thenReturn(List.of());
            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of());
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of());

            var output = service.calculateCashFlow();

            assertThat(output.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(output.totalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(output.balance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(output.pendingReceivablesCount()).isEqualTo(0);
            assertThat(output.pendingPayablesCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("calcula fluxo de caixa com receivedAmount null trata como zero")
        void cashFlow_nullReceivedAmount_treatedAsZero() {
            AccountReceivableEntity rec = overdueReceivable(UUID.randomUUID());
            rec.setStatus(AccountStatus.RECEIVED);
            rec.setReceivedAmount(null); // null should be treated as ZERO

            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.RECEIVED))
                    .thenReturn(List.of(rec));
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PAID))
                    .thenReturn(List.of());
            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of());
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of());

            var output = service.calculateCashFlow();

            assertThat(output.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
