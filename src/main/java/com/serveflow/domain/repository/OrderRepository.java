package com.serveflow.domain.repository;

import com.serveflow.domain.model.order.Order;
import com.serveflow.domain.model.order.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Order findById(UUID id);
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
}
