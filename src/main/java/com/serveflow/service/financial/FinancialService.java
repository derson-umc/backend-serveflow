package com.serveflow.service.financial;

import com.serveflow.dto.financial.request.AccountPayableInput;
import com.serveflow.dto.financial.request.AccountReceivableInput;
import com.serveflow.dto.financial.request.SettlementInput;
import com.serveflow.dto.financial.response.*;
import com.serveflow.exception.financial.AccountNotFoundException;
import com.serveflow.model.financial.*;
import com.serveflow.repository.financial.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class FinancialService {

    private final SpringAccountReceivableRepository receivableRepository;
    private final SpringAccountPayableRepository payableRepository;
    private final SpringFinancialAuditRepository auditRepository;

    public FinancialService(SpringAccountReceivableRepository receivableRepository,
                            SpringAccountPayableRepository payableRepository,
                            SpringFinancialAuditRepository auditRepository) {
        this.receivableRepository = receivableRepository;
        this.payableRepository = payableRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public AccountReceivableOutput createReceivable(AccountReceivableInput request) {
        AccountReceivable account = AccountReceivable.create(
                request.description(), request.dueDate(),
                request.amount(), request.category(), request.sourceOrderId());
        AccountReceivableEntity saved = receivableRepository.save(toReceivableEntity(account));
        recordAudit("ACCOUNTS_RECEIVABLE", saved.getId(), "CREATE", "system",
                "Receivable created: " + request.description());
        return toReceivableOutput(saved);
    }

    public List<AccountReceivableOutput> listReceivables() {
        return receivableRepository.findAllByOrderByDueDateAsc().stream()
                .map(this::toReceivableOutput).toList();
    }

    public AccountReceivableOutput findReceivable(UUID id) {
        return toReceivableOutput(receivableRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id)));
    }

    @Transactional
    public AccountReceivableOutput settleReceivable(UUID id, SettlementInput request) {
        AccountReceivableEntity entity = receivableRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        AccountReceivable account = toReceivableDomain(entity);
        account.registerReceipt(request.amount());
        updateReceivableEntity(entity, account);
        AccountReceivableEntity saved = receivableRepository.save(entity);
        recordAudit("ACCOUNTS_RECEIVABLE", id, "SETTLE", request.performedBy(),
                "Receipt of " + request.amount().toPlainString() + " recorded.");
        return toReceivableOutput(saved);
    }

    @Transactional
    public AccountReceivableOutput cancelReceivable(UUID id, String performedBy) {
        AccountReceivableEntity entity = receivableRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        AccountReceivable account = toReceivableDomain(entity);
        account.cancel();
        updateReceivableEntity(entity, account);
        AccountReceivableEntity saved = receivableRepository.save(entity);
        recordAudit("ACCOUNTS_RECEIVABLE", id, "CANCEL", performedBy, "Receivable cancelled.");
        return toReceivableOutput(saved);
    }

    @Transactional
    public AccountPayableOutput createPayable(AccountPayableInput request) {
        AccountPayable account = AccountPayable.create(
                request.description(), request.dueDate(),
                request.amount(), request.category(), request.supplier());
        AccountPayableEntity saved = payableRepository.save(toPayableEntity(account));
        recordAudit("ACCOUNTS_PAYABLE", saved.getId(), "CREATE", "system",
                "Payable created: " + request.description());
        return toPayableOutput(saved);
    }

    public List<AccountPayableOutput> listPayables() {
        return payableRepository.findAllByOrderByDueDateAsc().stream()
                .map(this::toPayableOutput).toList();
    }

    public AccountPayableOutput findPayable(UUID id) {
        return toPayableOutput(payableRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id)));
    }

    @Transactional
    public AccountPayableOutput settlePayable(UUID id, SettlementInput request) {
        AccountPayableEntity entity = payableRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        AccountPayable account = toPayableDomain(entity);
        account.registerPayment(request.amount());
        updatePayableEntity(entity, account);
        AccountPayableEntity saved = payableRepository.save(entity);
        recordAudit("ACCOUNTS_PAYABLE", id, "SETTLE", request.performedBy(),
                "Payment of " + request.amount().toPlainString() + " recorded.");
        return toPayableOutput(saved);
    }

    @Transactional
    public AccountPayableOutput cancelPayable(UUID id, String performedBy) {
        AccountPayableEntity entity = payableRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        AccountPayable account = toPayableDomain(entity);
        account.cancel();
        updatePayableEntity(entity, account);
        AccountPayableEntity saved = payableRepository.save(entity);
        recordAudit("ACCOUNTS_PAYABLE", id, "CANCEL", performedBy, "Payable cancelled.");
        return toPayableOutput(saved);
    }

    public CashFlowOutput calculateCashFlow() {
        List<AccountReceivableEntity> received = receivableRepository
                .findByStatusOrderByDueDateAsc(AccountStatus.RECEIVED);
        List<AccountPayableEntity> paid = payableRepository
                .findByStatusOrderByDueDateAsc(AccountStatus.PAID);
        List<AccountReceivableEntity> pendingReceivables = receivableRepository
                .findByStatusOrderByDueDateAsc(AccountStatus.PENDING);
        List<AccountPayableEntity> pendingPayables = payableRepository
                .findByStatusOrderByDueDateAsc(AccountStatus.PENDING);

        BigDecimal totalIncome = received.stream()
                .map(e -> e.getReceivedAmount() != null ? e.getReceivedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = paid.stream()
                .map(e -> e.getPaidAmount() != null ? e.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingReceivablesTotal = pendingReceivables.stream()
                .map(AccountReceivableEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingPayablesTotal = pendingPayables.stream()
                .map(AccountPayableEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CashFlowOutput(
                totalIncome, totalExpenses, totalIncome.subtract(totalExpenses),
                pendingReceivables.size(), pendingPayables.size(),
                pendingReceivablesTotal, pendingPayablesTotal);
    }

    public List<FinancialAuditOutput> listAudit() {
        return auditRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAuditOutput).toList();
    }

    public List<FinancialAuditOutput> listAuditByEntity(UUID entityId) {
        return auditRepository.findByEntityIdOrderByCreatedAtDesc(entityId).stream()
                .map(this::toAuditOutput).toList();
    }

    @Transactional
    public void processOverdueAccounts() {
        LocalDate today = LocalDate.now();
        receivableRepository.findByDueDateBeforeAndStatus(today, AccountStatus.PENDING)
                .forEach(e -> {
                    e.setStatus(AccountStatus.OVERDUE);
                    receivableRepository.save(e);
                });
        payableRepository.findByDueDateBeforeAndStatus(today, AccountStatus.PENDING)
                .forEach(e -> {
                    e.setStatus(AccountStatus.OVERDUE);
                    payableRepository.save(e);
                });
    }

    private void recordAudit(String entityType, UUID entityId,
                             String action, String performedBy, String description) {
        FinancialAuditEntity entry = new FinancialAuditEntity();
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setAction(action);
        entry.setPerformedBy(performedBy);
        entry.setDescription(description);
        auditRepository.save(entry);
    }

    private AccountReceivableEntity toReceivableEntity(AccountReceivable a) {
        AccountReceivableEntity e = new AccountReceivableEntity();
        e.setId(a.getId());
        e.setDescription(a.getDescription());
        e.setDueDate(a.getDueDate());
        e.setAmount(a.getAmount());
        e.setStatus(a.getStatus());
        e.setReceivedAt(a.getReceivedAt());
        e.setReceivedAmount(a.getReceivedAmount());
        e.setCategory(a.getCategory());
        e.setSourceOrderId(a.getSourceOrderId());
        return e;
    }

    private void updateReceivableEntity(AccountReceivableEntity entity, AccountReceivable a) {
        entity.setStatus(a.getStatus());
        entity.setReceivedAt(a.getReceivedAt());
        entity.setReceivedAmount(a.getReceivedAmount());
    }

    private AccountPayableEntity toPayableEntity(AccountPayable a) {
        AccountPayableEntity e = new AccountPayableEntity();
        e.setId(a.getId());
        e.setDescription(a.getDescription());
        e.setDueDate(a.getDueDate());
        e.setAmount(a.getAmount());
        e.setStatus(a.getStatus());
        e.setPaidAt(a.getPaidAt());
        e.setPaidAmount(a.getPaidAmount());
        e.setCategory(a.getCategory());
        e.setSupplier(a.getSupplier());
        return e;
    }

    private void updatePayableEntity(AccountPayableEntity entity, AccountPayable a) {
        entity.setStatus(a.getStatus());
        entity.setPaidAt(a.getPaidAt());
        entity.setPaidAmount(a.getPaidAmount());
    }

    private AccountReceivable toReceivableDomain(AccountReceivableEntity e) {
        return new AccountReceivable(e.getId(), e.getDescription(), e.getDueDate(),
                e.getAmount(), e.getStatus(), e.getReceivedAt(), e.getReceivedAmount(),
                e.getCategory(), e.getSourceOrderId(), e.getCreatedAt(), e.getVersion());
    }

    private AccountPayable toPayableDomain(AccountPayableEntity e) {
        return new AccountPayable(e.getId(), e.getDescription(), e.getDueDate(),
                e.getAmount(), e.getStatus(), e.getPaidAt(), e.getPaidAmount(),
                e.getCategory(), e.getSupplier(), e.getCreatedAt(), e.getVersion());
    }

    private AccountReceivableOutput toReceivableOutput(AccountReceivableEntity e) {
        return new AccountReceivableOutput(e.getId(), e.getDescription(), e.getDueDate(),
                e.getAmount(), e.getStatus().name(), e.getReceivedAt(), e.getReceivedAmount(),
                e.getCategory(), e.getSourceOrderId(), e.getCreatedAt());
    }

    private AccountPayableOutput toPayableOutput(AccountPayableEntity e) {
        return new AccountPayableOutput(e.getId(), e.getDescription(), e.getDueDate(),
                e.getAmount(), e.getStatus().name(), e.getPaidAt(), e.getPaidAmount(),
                e.getCategory(), e.getSupplier(), e.getCreatedAt());
    }

    private FinancialAuditOutput toAuditOutput(FinancialAuditEntity e) {
        return new FinancialAuditOutput(e.getId(), e.getEntityType(),
                e.getEntityId(), e.getAction(), e.getPerformedBy(),
                e.getDescription(), e.getCreatedAt());
    }
}
