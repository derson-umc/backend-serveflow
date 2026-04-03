package com.serveflow.domain.service;

import com.serveflow.domain.model.order.Order;
import com.serveflow.domain.model.order.OrderItem;
import com.serveflow.domain.model.order.OrderStatus;
import com.serveflow.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order create(Order order, List<OrderItem> items) {
        items.forEach(order::addItem);
        return orderRepository.save(order);
    }

    public Order findById(UUID id) {
        return orderRepository.findById(id);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order startPreparation(UUID id) {
        return updateOrder(id, Order::startPreparation);
    }

    @Transactional
    public Order markReady(UUID id) {
        return updateOrder(id, Order::markReady);
    }

    @Transactional
    public Order sendForDelivery(UUID id) {
        return updateOrder(id, Order::sendForDelivery);
    }

    @Transactional
    public Order complete(UUID id) {
        return updateOrder(id, Order::complete);
    }

    @Transactional
    public Order cancel(UUID id) {
        return updateOrder(id, Order::cancel);
    }

    @Transactional
    public Order addItem(UUID id, OrderItem item) {
        return updateOrder(id, order -> order.addItem(item));
    }

    @Transactional
    public Order removeItem(UUID orderId, OrderItem item) {
        return updateOrder(orderId, order -> order.removeItem(item));
    }

    private Order updateOrder(UUID id, Consumer<Order> action) {
        Order order = orderRepository.findById(id);
        action.accept(order);
        return orderRepository.save(order);
    }
}
