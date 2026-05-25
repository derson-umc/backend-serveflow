package com.serveflow.service.financial;

import com.serveflow.dto.financial.request.AccountPayableInput;
import com.serveflow.dto.financial.request.AccountReceivableInput;
import com.serveflow.dto.financial.request.SettlementInput;
import com.serveflow.dto.financial.response.AccountPayableOutput;
import com.serveflow.dto.financial.response.AccountReceivableOutput;
import com.serveflow.dto.financial.response.CashFlowOutput;
import com.serveflow.exception.financial.AccountNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FinancialServiceTest {

    @Mock SpringAccountReceivableRepository receivableRepository;
    @Mock SpringAccountPayableRepository payableRepository;
    @Mock SpringFinancialAuditRepository auditRepository;

    @InjectMocks FinancialService service;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        when(auditRepository.save(any(FinancialAuditEntity.class))).thenReturn(new FinancialAuditEntity());
    }

    @Nested
    @DisplayName("createReceivable")
    class CreateReceivable {

        @Test
        @DisplayName("persiste conta a receber e registra auditoria.")
        void createReceivable_persisteEAudita() {
            AccountReceivableEntity saved = receivableEntity(accountId, AccountStatus.PENDING, null, null);
            when(receivableRepository.save(any(AccountReceivableEntity.class))).thenReturn(saved);

            AccountReceivableOutput result = service.createReceivable(receivableInput());

            assertThat(result.id()).isEqualTo(accountId);
            assertThat(result.status()).isEqualTo("PENDING");
            verify(receivableRepository).save(any(AccountReceivableEntity.class));
            verify(auditRepository).save(any(FinancialAuditEntity.class));
        }

        @Test
        @DisplayName("cria conta com descrição e valor corretos.")
        void createReceivable_propagaCamposCorretamente() {
            AccountReceivableEntity saved = receivableEntity(accountId, AccountStatus.PENDING, null, null);
            saved.setDescription("Venda Mesa 5");
            saved.setAmount(new BigDecimal("120.00"));
            when(receivableRepository.save(any(AccountReceivableEntity.class))).thenReturn(saved);

            AccountReceivableOutput result = service.createReceivable(
                    new AccountReceivableInput("Venda Mesa 5", LocalDate.now().plusDays(7),
                            new BigDecimal("120.00"), "VENDAS", null));

            assertThat(result.description()).isEqualTo("Venda Mesa 5");
            assertThat(result.amount()).isEqualByComparingTo("120.00");
        }
    }

    @Nested
    @DisplayName("findReceivable")
    class FindReceivable {

        @Test
        @DisplayName("retorna output quando conta existe.")
        void findReceivable_retornaOutput_whenEncontrada() {
            AccountReceivableEntity entity = receivableEntity(accountId, AccountStatus.PENDING, null, null);
            when(receivableRepository.findById(accountId)).thenReturn(Optional.of(entity));

            AccountReceivableOutput result = service.findReceivable(accountId);

            assertThat(result.id()).isEqualTo(accountId);
        }

        @Test
        @DisplayName("lança AccountNotFoundException quando ID não existe.")
        void findReceivable_lancaExcecao_whenNaoEncontrada() {
            when(receivableRepository.findById(accountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findReceivable(accountId))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("settleReceivable")
    class SettleReceivable {

        @Test
        @DisplayName("liquida conta a receber, atualiza status e registra auditoria.")
        void settleReceivable_liquidaEAudita() {
            AccountReceivableEntity entity = receivableEntity(accountId, AccountStatus.PENDING, null, null);
            when(receivableRepository.findById(accountId)).thenReturn(Optional.of(entity));
            AccountReceivableEntity settled = receivableEntity(accountId, AccountStatus.RECEIVED,
                    LocalDateTime.now(), new BigDecimal("100.00"));
            when(receivableRepository.save(entity)).thenReturn(settled);

            AccountReceivableOutput result = service.settleReceivable(accountId,
                    new SettlementInput(new BigDecimal("100.00"), "caixa01"));

            assertThat(result.status()).isEqualTo("RECEIVED");
            assertThat(result.receivedAmount()).isEqualByComparingTo("100.00");
            verify(auditRepository, times(1)).save(any(FinancialAuditEntity.class));
        }

        @Test
        @DisplayName("lança AccountNotFoundException quando conta não existe.")
        void settleReceivable_lancaExcecao_whenNaoEncontrada() {
            when(receivableRepository.findById(accountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.settleReceivable(accountId,
                    new SettlementInput(BigDecimal.TEN, "user")))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cancelReceivable")
    class CancelReceivable {

        @Test
        @DisplayName("cancela conta a receber e registra auditoria.")
        void cancelReceivable_cancelaEAudita() {
            AccountReceivableEntity entity = receivableEntity(accountId, AccountStatus.PENDING, null, null);
            when(receivableRepository.findById(accountId)).thenReturn(Optional.of(entity));
            AccountReceivableEntity cancelled = receivableEntity(accountId, AccountStatus.CANCELLED, null, null);
            when(receivableRepository.save(entity)).thenReturn(cancelled);

            AccountReceivableOutput result = service.cancelReceivable(accountId, "supervisor");

            assertThat(result.status()).isEqualTo("CANCELLED");
            verify(auditRepository).save(any(FinancialAuditEntity.class));
        }
    }

    @Nested
    @DisplayName("createPayable")
    class CreatePayable {

        @Test
        @DisplayName("persiste conta a pagar e registra auditoria.")
        void createPayable_persisteEAudita() {
            AccountPayableEntity saved = payableEntity(accountId, AccountStatus.PENDING, null, null);
            when(payableRepository.save(any(AccountPayableEntity.class))).thenReturn(saved);

            AccountPayableOutput result = service.createPayable(payableInput());

            assertThat(result.id()).isEqualTo(accountId);
            assertThat(result.status()).isEqualTo("PENDING");
            verify(payableRepository).save(any(AccountPayableEntity.class));
            verify(auditRepository).save(any(FinancialAuditEntity.class));
        }
    }

    @Nested
    @DisplayName("settlePayable")
    class SettlePayable {

        @Test
        @DisplayName("liquida conta a pagar, atualiza status e registra auditoria.")
        void settlePayable_liquidaEAudita() {
            AccountPayableEntity entity = payableEntity(accountId, AccountStatus.PENDING, null, null);
            when(payableRepository.findById(accountId)).thenReturn(Optional.of(entity));
            AccountPayableEntity settled = payableEntity(accountId, AccountStatus.PAID,
                    LocalDateTime.now(), new BigDecimal("200.00"));
            when(payableRepository.save(entity)).thenReturn(settled);

            AccountPayableOutput result = service.settlePayable(accountId,
                    new SettlementInput(new BigDecimal("200.00"), "financeiro"));

            assertThat(result.status()).isEqualTo("PAID");
            assertThat(result.paidAmount()).isEqualByComparingTo("200.00");
            verify(auditRepository).save(any(FinancialAuditEntity.class));
        }

        @Test
        @DisplayName("lança AccountNotFoundException quando conta a pagar não existe.")
        void settlePayable_lancaExcecao_whenNaoEncontrada() {
            when(payableRepository.findById(accountId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.settlePayable(accountId,
                    new SettlementInput(BigDecimal.TEN, "user")))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("calculateCashFlow")
    class CalculateCashFlow {

        @Test
        @DisplayName("calcula saldo correto com receitas e despesas realizadas.")
        void calculateCashFlow_calculaSaldoCorretamente() {
            AccountReceivableEntity r1 = receivableEntity(UUID.randomUUID(), AccountStatus.RECEIVED,
                    LocalDateTime.now(), new BigDecimal("300.00"));
            AccountReceivableEntity r2 = receivableEntity(UUID.randomUUID(), AccountStatus.RECEIVED,
                    LocalDateTime.now(), new BigDecimal("200.00"));
            AccountPayableEntity p1 = payableEntity(UUID.randomUUID(), AccountStatus.PAID,
                    LocalDateTime.now(), new BigDecimal("150.00"));

            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.RECEIVED))
                    .thenReturn(List.of(r1, r2));
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PAID))
                    .thenReturn(List.of(p1));
            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of());
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of());

            CashFlowOutput result = service.calculateCashFlow();

            assertThat(result.totalIncome()).isEqualByComparingTo("500.00");
            assertThat(result.totalExpenses()).isEqualByComparingTo("150.00");
            assertThat(result.balance()).isEqualByComparingTo("350.00");
        }

        @Test
        @DisplayName("retorna saldo zero quando não há movimentações realizadas.")
        void calculateCashFlow_retornaZero_whenSemMovimentacoes() {
            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.RECEIVED))
                    .thenReturn(List.of());
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PAID))
                    .thenReturn(List.of());
            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of());
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of());

            CashFlowOutput result = service.calculateCashFlow();

            assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.totalExpenses()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.balance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.pendingReceivablesCount()).isZero();
            assertThat(result.pendingPayablesCount()).isZero();
        }

        @Test
        @DisplayName("contabiliza corretamente contas pendentes a receber e a pagar.")
        void calculateCashFlow_contabilizaPendentes() {
            AccountReceivableEntity pending1 = receivableEntity(UUID.randomUUID(), AccountStatus.PENDING, null, null);
            pending1.setAmount(new BigDecimal("400.00"));
            AccountPayableEntity pendingP1 = payableEntity(UUID.randomUUID(), AccountStatus.PENDING, null, null);
            pendingP1.setAmount(new BigDecimal("180.00"));

            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.RECEIVED)).thenReturn(List.of());
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PAID)).thenReturn(List.of());
            when(receivableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of(pending1));
            when(payableRepository.findByStatusOrderByDueDateAsc(AccountStatus.PENDING))
                    .thenReturn(List.of(pendingP1));

            CashFlowOutput result = service.calculateCashFlow();

            assertThat(result.pendingReceivablesCount()).isEqualTo(1);
            assertThat(result.pendingPayablesCount()).isEqualTo(1);
            assertThat(result.pendingReceivablesTotal()).isEqualByComparingTo("400.00");
            assertThat(result.pendingPayablesTotal()).isEqualByComparingTo("180.00");
        }
    }

    @Nested
    @DisplayName("processOverdueAccounts")
    class ProcessOverdueAccounts {

        @Test
        @DisplayName("marca contas vencidas como OVERDUE para receivables e payables.")
        void processOverdueAccounts_marcaComoVencido() {
            AccountReceivableEntity overdue = receivableEntity(accountId, AccountStatus.PENDING, null, null);
            overdue.setDueDate(LocalDate.now().minusDays(1));
            AccountPayableEntity overdueP = payableEntity(accountId, AccountStatus.PENDING, null, null);
            overdueP.setDueDate(LocalDate.now().minusDays(1));

            when(receivableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), any()))
                    .thenReturn(List.of(overdue));
            when(payableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), any()))
                    .thenReturn(List.of(overdueP));

            service.processOverdueAccounts();

            verify(receivableRepository).save(overdue);
            verify(payableRepository).save(overdueP);
            assertThat(overdue.getStatus()).isEqualTo(AccountStatus.OVERDUE);
            assertThat(overdueP.getStatus()).isEqualTo(AccountStatus.OVERDUE);
        }

        @Test
        @DisplayName("não executa saves quando não há contas vencidas.")
        void processOverdueAccounts_naoSalva_whenSemVencidos() {
            when(receivableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), any()))
                    .thenReturn(List.of());
            when(payableRepository.findByDueDateBeforeAndStatus(any(LocalDate.class), any()))
                    .thenReturn(List.of());

            service.processOverdueAccounts();

            verify(receivableRepository, org.mockito.Mockito.never()).save(any());
            verify(payableRepository, org.mockito.Mockito.never()).save(any());
        }
    }

    private AccountReceivableEntity receivableEntity(UUID id, AccountStatus status,
                                                     LocalDateTime receivedAt, BigDecimal receivedAmount) {
        AccountReceivableEntity e = new AccountReceivableEntity();
        e.setId(id);
        e.setDescription("Conta teste");
        e.setDueDate(LocalDate.now().plusDays(30));
        e.setAmount(new BigDecimal("100.00"));
        e.setStatus(status);
        e.setReceivedAt(receivedAt);
        e.setReceivedAmount(receivedAmount);
        e.setCategory("VENDAS");
        return e;
    }

    private AccountPayableEntity payableEntity(UUID id, AccountStatus status,
                                               LocalDateTime paidAt, BigDecimal paidAmount) {
        AccountPayableEntity e = new AccountPayableEntity();
        e.setId(id);
        e.setDescription("Despesa teste");
        e.setDueDate(LocalDate.now().plusDays(30));
        e.setAmount(new BigDecimal("100.00"));
        e.setStatus(status);
        e.setPaidAt(paidAt);
        e.setPaidAmount(paidAmount);
        e.setCategory("FORNECEDORES");
        return e;
    }

    private AccountReceivableInput receivableInput() {
        return new AccountReceivableInput("Conta a receber", LocalDate.now().plusDays(30),
                new BigDecimal("100.00"), "VENDAS", null);
    }

    private AccountPayableInput payableInput() {
        return new AccountPayableInput("Conta a pagar", LocalDate.now().plusDays(30),
                new BigDecimal("100.00"), "FORNECEDORES", "Fornecedor ABC");
    }
}
