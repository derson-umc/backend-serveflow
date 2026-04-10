package com.serveflow.data.repository.stock;

import com.serveflow.data.mapper.StockMapper;
import com.serveflow.domain.model.stock.StockMovement;
import com.serveflow.domain.repository.StockMovementRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class StockMovementRepositoryImpl implements StockMovementRepository {

    private final SpringStockMovementRepository springRepository;
    private final StockMapper mapper;

    public StockMovementRepositoryImpl(SpringStockMovementRepository springRepository, StockMapper mapper) {
        this.springRepository = springRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public StockMovement save(StockMovement movement) {
        var entity = mapper.toEntity(movement);
        return mapper.toDomain(springRepository.save(entity));
    }

    @Override
    public List<StockMovement> findByStockItemId(UUID stockItemId) {
        return springRepository.findByStockItemId(stockItemId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<StockMovement> findByReferenceId(UUID referenceId) {
        return springRepository.findByReferenceId(referenceId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
