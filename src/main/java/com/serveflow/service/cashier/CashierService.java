package com.serveflow.service.cashier;

import com.serveflow.dto.cashier.request.CashMovementInput;
import com.serveflow.dto.cashier.request.CloseSessionInput;
import com.serveflow.dto.cashier.request.OpenSessionInput;
import com.serveflow.dto.cashier.response.CashMovementOutput;
import com.serveflow.dto.cashier.response.CashSessionOutput;
import com.serveflow.exception.cashier.CashSessionNotFoundException;
import com.serveflow.exception.cashier.OpenSessionAlreadyExistsException;
import com.serveflow.model.cashier.CashSession;
import com.serveflow.model.cashier.CashSessionStatus;
import com.serveflow.repository.cashier.CashMovementEntity;
import com.serveflow.repository.cashier.CashSessionEntity;
import com.serveflow.repository.cashier.SpringCashMovementRepository;
import com.serveflow.repository.cashier.SpringCashSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CashierService {

    private final SpringCashSessionRepository sessionRepository;
    private final SpringCashMovementRepository movementRepository;

    public CashierService(SpringCashSessionRepository sessionRepository,
                          SpringCashMovementRepository movementRepository) {
        this.sessionRepository = sessionRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public CashSessionOutput openSession(OpenSessionInput request) {
        if (sessionRepository.existsByStatus(CashSessionStatus.OPEN)) {
            throw new OpenSessionAlreadyExistsException();
        }
        CashSession session = CashSession.open(request.initialBalance(), request.observation(), request.openedBy());
        CashSessionEntity saved = sessionRepository.save(toEntity(session));
        return toOutput(saved);
    }

    @Transactional
    public CashSessionOutput closeSession(UUID id, CloseSessionInput request) {
        CashSessionEntity entity = sessionRepository.findById(id)
                .orElseThrow(() -> new CashSessionNotFoundException(id));
        CashSession session = toDomain(entity);
        session.close(request.closedBy(), request.closingObservation());
        updateEntity(entity, session);
        return toOutput(sessionRepository.save(entity));
    }

    public Optional<CashSessionOutput> getCurrentSession() {
        return sessionRepository.findFirstByStatusOrderByOpenedAtDesc(CashSessionStatus.OPEN)
                .map(this::toOutput);
    }

    public List<CashSessionOutput> listSessions() {
        return sessionRepository.findAllByOrderByOpenedAtDesc().stream()
                .map(this::toOutput).toList();
    }

    @Transactional
    public CashMovementOutput addMovement(UUID sessionId, CashMovementInput request) {
        CashSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CashSessionNotFoundException(sessionId));
        if (session.getStatus() == CashSessionStatus.CLOSED) {
            throw new com.serveflow.exception.cashier.CashSessionAlreadyClosedException(sessionId);
        }
        CashMovementEntity entity = new CashMovementEntity();
        entity.setSessionId(sessionId);
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setDescription(request.description());
        entity.setCategory(request.category());
        entity.setPerformedBy(request.performedBy());
        return toMovementOutput(movementRepository.save(entity));
    }

    public List<CashMovementOutput> listMovements(UUID sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new CashSessionNotFoundException(sessionId);
        }
        return movementRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toMovementOutput).toList();
    }


    private CashSessionEntity toEntity(CashSession s) {
        CashSessionEntity e = new CashSessionEntity();
        e.setId(s.getId());
        e.setStatus(s.getStatus());
        e.setInitialBalance(s.getInitialBalance());
        e.setObservation(s.getObservation());
        e.setOpenedBy(s.getOpenedBy());
        return e;
    }

    private void updateEntity(CashSessionEntity entity, CashSession s) {
        entity.setStatus(s.getStatus());
        entity.setClosedAt(s.getClosedAt());
        entity.setClosedBy(s.getClosedBy());
        entity.setClosingObservation(s.getClosingObservation());
    }

    private CashSession toDomain(CashSessionEntity e) {
        return new CashSession(e.getId(), e.getStatus(), e.getInitialBalance(),
                e.getObservation(), e.getOpenedAt(), e.getClosedAt(),
                e.getOpenedBy(), e.getClosedBy(), e.getClosingObservation(), e.getVersion());
    }

    private CashSessionOutput toOutput(CashSessionEntity e) {
        return new CashSessionOutput(e.getId(), e.getStatus().name(), e.getInitialBalance(),
                e.getObservation(), e.getOpenedAt(), e.getClosedAt(),
                e.getOpenedBy(), e.getClosedBy(), e.getClosingObservation());
    }

    private CashMovementOutput toMovementOutput(CashMovementEntity e) {
        return new CashMovementOutput(e.getId(), e.getSessionId(), e.getType().name(),
                e.getAmount(), e.getDescription(), e.getCategory(),
                e.getPerformedBy(), e.getCreatedAt());
    }
}
