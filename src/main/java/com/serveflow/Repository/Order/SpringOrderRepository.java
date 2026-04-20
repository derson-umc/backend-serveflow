package com.serveflow.Repository.Order;

import com.serveflow.Model.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringOrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByStatus(OrderStatus status);
}
