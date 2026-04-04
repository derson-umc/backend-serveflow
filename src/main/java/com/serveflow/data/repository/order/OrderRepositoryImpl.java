package com.serveflow.data.repository.order;

import com.serveflow.data.entity.order.OrderEntity;
import com.serveflow.data.mapper.OrderMapper;
import com.serveflow.domain.exception.OrderNotFoundException;
import com.serveflow.domain.model.order.Order;
import com.serveflow.domain.model.order.OrderStatus;
import com.serveflow.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class OrderRepositoryImpl implements OrderRepository {

    private final SpringOrderRepository springRepository;
    private final OrderMapper mapper;

    public OrderRepositoryImpl(SpringOrderRepository springRepository, OrderMapper mapper) {
        this.springRepository = springRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        boolean isNewOrder = order.getVersion() == null;
        OrderEntity entity;

        if (isNewOrder) {
            entity = mapper.toEntity(order);
        } else {
            entity = springRepository.findById(order.getId())
                    .orElseThrow(() -> new OrderNotFoundException(order.getId()));
            mapper.updateEntity(entity, order);
        }

        var saved = springRepository.save(entity);
        return mapper.toDomain(saved);
    }
    @Override
    public Order findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public List<Order> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        var entityStatus = OrderEntity.OrderStatus.valueOf(status.name());
        return springRepository.findByStatus(entityStatus).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
