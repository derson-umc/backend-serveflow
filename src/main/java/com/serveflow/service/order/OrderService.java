package com.serveflow.service.order;

import com.serveflow.dto.order.request.*;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.integration.AddressResolver;
import com.serveflow.model.address.Address;
import com.serveflow.model.order.*;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.order.OrderRepository;
import com.serveflow.service.stock.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressResolver addressResolver;
    private final StockService stockService;
    private final MenuRepository menuRepository;

    public OrderService(OrderRepository orderRepository,
                        AddressResolver addressResolver,
                        StockService stockService,
                        MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.addressResolver = addressResolver;
        this.stockService = stockService;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public OrderOutput create(OrderInput request) {
        Address resolvedAddress = addressResolver.resolve(request.address());
        OrderType orderType = OrderType.valueOf(request.type().toUpperCase());

        Order order = Order.create(request.customerName(), resolvedAddress, orderType, request.observation());
        toItems(request.items()).forEach(order::addItem);

        return toOutput(orderRepository.save(order));
    }

    public OrderOutput findById(UUID id) {
        return toOutput(orderRepository.findById(id));
    }

    public List<OrderOutput> findAll() {
        return orderRepository.findAll().stream().map(this::toOutput).toList();
    }

    public List<OrderOutput> findByStatus(String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus).stream().map(this::toOutput).toList();
    }

    @Transactional
    public OrderOutput confirm(UUID id) {
        Order order = orderRepository.findById(id);
        stockService.validateStockForOrder(order.getItems());
        order.confirm();
        Order saved = orderRepository.save(order);
        stockService.deductForOrder(saved.getId(), saved.getItems());
        return toOutput(saved);
    }

    @Transactional
    public OrderOutput startPreparation(UUID id) {
        return toOutput(transition(id, Order::startPreparation));
    }

    @Transactional
    public OrderOutput markReady(UUID id) {
        return toOutput(transition(id, Order::markReady));
    }

    @Transactional
    public OrderOutput sendForDelivery(UUID id) {
        return toOutput(transition(id, Order::sendForDelivery));
    }

    @Transactional
    public OrderOutput complete(UUID id) {
        Order saved = transition(id, Order::complete);
        unlockMenuByOrder(saved.getId());
        return toOutput(saved);
    }

    @Transactional
    public OrderOutput cancel(UUID id) {
        Order order = orderRepository.findById(id);
        boolean stockWasDeducted = order.getStatus() != OrderStatus.CREATED;
        order.cancel();
        Order saved = orderRepository.save(order);
        if (stockWasDeducted) {
            stockService.restoreForOrder(saved.getId(), saved.getItems());
        }
        unlockMenuByOrder(saved.getId());
        return toOutput(saved);
    }

    private void unlockMenuByOrder(UUID orderId) {
        menuRepository.findByActiveOrderId(orderId).ifPresent(menu -> {
            menu.unlock();
            menuRepository.save(menu);
        });
    }

    private List<OrderItem> toItems(List<OrderItemInput> inputs) {
        return inputs.stream().map(this::toItem).toList();
    }

    private OrderItem toItem(OrderItemInput dto) {
        List<ItemAdditional> additionals = Optional.ofNullable(dto.additionals())
                .orElse(List.of()).stream()
                .map(a -> new ItemAdditional(a.name(), a.quantity(), a.unitPrice()))
                .toList();
        return new OrderItem(dto.productId(), dto.productName(), dto.quantity(),
                dto.unitPrice(), dto.observation(), additionals);
    }

    private OrderOutput toOutput(Order order) {
        return new OrderOutput(
                order.getId(),
                order.getCustomerName(),
                OrderOutput.AddressOutput.from(order.getAddress()),
                order.getType().name(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getObservation(),
                order.getTotal(),
                order.getItems().stream().map(this::toItemOutput).toList()
        );
    }

    private OrderOutput.OrderItemOutput toItemOutput(OrderItem item) {
        return new OrderOutput.OrderItemOutput(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getObservation(),
                item.getTotal(),
                item.getAdditionals().stream().map(a ->
                        new OrderOutput.ItemAdditionalOutput(
                                a.getId(), a.getName(), a.getQuantity(), a.getUnitPrice(), a.getTotal())
                ).toList()
        );
    }

    private Order transition(UUID id, Consumer<Order> action) {
        Order order = orderRepository.findById(id);
        action.accept(order);
        return orderRepository.save(order);
    }
}
