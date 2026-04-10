package com.serveflow.domain.service;

import com.serveflow.domain.event.DomainEventPublisher;
import com.serveflow.domain.event.OrderConfirmedEvent;
import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.model.order.Order;
import com.serveflow.domain.model.order.OrderItem;
import com.serveflow.domain.model.order.OrderStatus;
import com.serveflow.domain.repository.AddressRepository;
import com.serveflow.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressLookupService;
    private final DomainEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        AddressRepository addressLookupService,
                        DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.addressLookupService = addressLookupService;
        this.eventPublisher = eventPublisher;
    }

    public Address resolveAddress(String cep, String number, String complement, Address manualAddress) {
        if (cep != null && !cep.isBlank()) {
            return addressLookupService.findByCep(cep)
                    .map(resolved -> new Address(
                            resolved.getCep(),
                            resolved.getStreet(),
                            resolved.getCity(),
                            resolved.getState(),
                            number != null ? number : "",
                            complement
                    ))
                    .orElse(manualAddress);
        }
        return manualAddress;
    }

    @Transactional
    public Order create(Order order) {
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
    public Order confirm(UUID id) {
        Order order = orderRepository.findById(id);
        order.confirm();
        Order saved = orderRepository.save(order);

        var itemSnapshots = saved.getItems().stream()
                .map(item -> new OrderConfirmedEvent.OrderItemSnapshot(
                        item.getProductId(), item.getProductName(), item.getQuantity()))
                .toList();

        eventPublisher.publish(new OrderConfirmedEvent(
                saved.getId(), itemSnapshots, LocalDateTime.now()));

        return saved;
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
