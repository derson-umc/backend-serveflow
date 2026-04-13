package com.serveflow.data.repository.order;

import com.serveflow.data.entity.order.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.serveflow.domain.model.order.OrderStatus;


import java.util.List;
import java.util.UUID;

public interface SpringOrderRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findByStatus(OrderStatus status);
}

