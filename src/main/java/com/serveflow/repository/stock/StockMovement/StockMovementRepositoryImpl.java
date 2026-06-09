package com.serveflow.repository.stock.stockmovement;

import com.serveflow.dto.stock.response.StockConsolidatedOutput;
import com.serveflow.dto.stock.response.StockMovementOutput;
import com.serveflow.dto.stock.response.StockMovementsPageOutput;
import com.serveflow.model.stock.MovementType;
import com.serveflow.model.stock.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class StockMovementRepositoryImpl implements StockMovementRepository {

    private final SpringStockMovementRepository springRepository;

    public StockMovementRepositoryImpl(SpringStockMovementRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    @Transactional
    public StockMovement save(StockMovement movement) {
        return toDomain(springRepository.save(toEntity(movement)));
    }

    @Override
    public List<StockMovement> findAll() {
        return springRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<StockMovement> findByStockItemId(UUID stockItemId) {
        return springRepository.findByStockItemIdOrderByCreatedAtDesc(stockItemId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<StockMovement> findByReferenceId(UUID referenceId) {
        return springRepository.findByReferenceId(referenceId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<StockConsolidatedOutput> findConsolidatedReport() {
        return springRepository.findConsolidatedReport().stream()
                .map(row -> new StockConsolidatedOutput(
                        row.getInsumo(), row.getUnidade(),
                        row.getTotalEntradas(), row.getTotalSaidas(), row.getSaldoAtual()))
                .toList();
    }

    @Override
    public StockMovementsPageOutput findFiltered(UUID stockItemId, MovementType type, LocalDate start, LocalDate end, int page, int size) {
        Specification<StockMovementEntity> spec = Specification.where(null);

        if (stockItemId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("stockItemId"), stockItemId));
        }
        if (type != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("type"), type));
        }
        if (start != null) {
            LocalDateTime startDt = start.atStartOfDay();
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDt));
        }
        if (end != null) {
            LocalDateTime endDt = end.plusDays(1).atStartOfDay();
            spec = spec.and((root, q, cb) -> cb.lessThan(root.get("createdAt"), endDt));
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockMovementEntity> result = springRepository.findAll(spec, pageRequest);
        List<StockMovementOutput> content = result.getContent().stream()
                .map(e -> new StockMovementOutput(
                        e.getIdMovement(), e.getStockItemId(), e.getStockItemName(),
                        e.getType().name(), e.getType().getDescription(),
                        e.getQuantity(), e.getBalanceBefore(), e.getBalanceAfter(),
                        e.getReason(), e.getReferenceId(), e.getCreatedAt()))
                .toList();
        return new StockMovementsPageOutput(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private StockMovement toDomain(StockMovementEntity e) {
        return new StockMovement(
                e.getIdMovement(), e.getStockItemId(), e.getStockItemName(),
                MovementType.valueOf(e.getType().name()),
                e.getQuantity(), e.getBalanceBefore(), e.getBalanceAfter(),
                e.getReason(), e.getReferenceId(), e.getCreatedAt());
    }

    private StockMovementEntity toEntity(StockMovement m) {
        StockMovementEntity e = new StockMovementEntity();
        e.setIdMovement(m.getId());
        e.setStockItemId(m.getStockItemId());
        e.setStockItemName(m.getStockItemName());
        e.setType(MovementType.valueOf(m.getType().name()));
        e.setQuantity(m.getQuantity());
        e.setBalanceBefore(m.getBalanceBefore());
        e.setBalanceAfter(m.getBalanceAfter());
        e.setReason(m.getReason());
        e.setReferenceId(m.getReferenceId());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }
}
