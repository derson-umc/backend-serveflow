package com.serveflow.service.order;

import com.serveflow.controller.kds.KdsEventPublisher;
import com.serveflow.dto.kds.response.KdsMapper;
import com.serveflow.dto.order.request.*;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.events.OrderCompletedEvent;
import com.serveflow.integration.AddressResolver;
import com.serveflow.model.address.Address;
import com.serveflow.model.order.*;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.order.OrderRepository;
import com.serveflow.service.stock.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository           orderRepository;
    private final AddressResolver           addressResolver;
    private final StockService              stockService;
    private final MenuRepository            menuRepository;
    private final KdsEventPublisher         kdsEventPublisher;
    private final ApplicationEventPublisher eventPublisher;
    private final KdsMapper                 kdsMapper;

    public OrderService(OrderRepository orderRepository,
                        AddressResolver addressResolver,
                        StockService stockService,
                        MenuRepository menuRepository,
                        @Lazy KdsEventPublisher kdsEventPublisher,
                        ApplicationEventPublisher eventPublisher,
                        KdsMapper kdsMapper) {
        this.orderRepository   = orderRepository;
        this.addressResolver   = addressResolver;
        this.stockService      = stockService;
        this.menuRepository    = menuRepository;
        this.kdsEventPublisher = kdsEventPublisher;
        this.eventPublisher    = eventPublisher;
        this.kdsMapper         = kdsMapper;
    }

    @Transactional
    public OrderOutput create(OrderInput request) {
        Address resolvedAddress = addressResolver.resolve(request.address());
        OrderType orderType = OrderType.valueOf(request.type().toUpperCase());

        List<OrderItem> items = toItems(request.items());
        stockService.validateRecipesForOrder(items);

        Order order = Order.create(request.customerName(), resolvedAddress, orderType, request.observation(), request.tableNumber());
        items.forEach(order::addItem);

        if (request.paymentMethod() != null && !request.paymentMethod().isBlank()) {
            order.registerPayment(request.paymentMethod());
        }

        OrderOutput output = toOutput(orderRepository.save(order));
        publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        return output;
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
        OrderOutput output = toOutput(saved);
        publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        return output;
    }

    @Transactional
    public OrderOutput startPreparation(UUID id) {
        Order order = orderRepository.findById(id);
        if (order.getStatus() == OrderStatus.RASCUNHO) {
            stockService.validateRecipesForOrder(order.getItems());
            stockService.validateStockForOrder(order.getItems());
            order.confirm();
            order = orderRepository.save(order);
            stockService.deductForOrder(order.getId(), order.getItems());
        }
        order.startPreparation();
        OrderOutput output = toOutput(orderRepository.save(order));
        publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        return output;
    }

    @Transactional
    public OrderOutput markReady(UUID id) {
        OrderOutput output = toOutput(transition(id, Order::markReady));
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(output.id()));
        return output;
    }

    @Transactional
    public OrderOutput sendForDelivery(UUID id) {
        OrderOutput output = toOutput(transition(id, Order::sendForDelivery));
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(output.id()));
        return output;
    }

    @Transactional
    public OrderOutput complete(UUID id) {
        Order saved = transition(id, Order::complete);
        unlockMenuByOrder(saved.getId());
        OrderOutput output = toOutput(saved);
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(output.id()));
        eventPublisher.publishEvent(new OrderCompletedEvent(
                saved.getId(),
                saved.getCustomerName(),
                saved.getType().name(),
                saved.getPaymentMethod(),
                saved.getTotal()
        ));
        return output;
    }

    @Transactional
    public OrderOutput settleFromCashier(UUID id, String paymentMethod) {
        Order order = orderRepository.findById(id);
        order.registerPayment(paymentMethod);
        order.complete();
        Order saved = orderRepository.save(order);
        unlockMenuByOrder(saved.getId());
        OrderOutput output = toOutput(saved);
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(output.id()));
        eventPublisher.publishEvent(new OrderCompletedEvent(
                saved.getId(),
                saved.getCustomerName(),
                saved.getType().name(),
                saved.getPaymentMethod(),
                saved.getTotal()
        ));
        return output;
    }

    @Transactional
    public OrderOutput cancel(UUID id, String reason, String canceledBy) {
        log.debug("Cancelando pedido id={}", id);
        Order order = orderRepository.findById(id);
        log.debug("Pedido encontrado: status={}, itens={}", order.getStatus(), order.getItems().size());
        boolean stockWasDeducted = order.getStatus() != OrderStatus.RASCUNHO;
        order.cancel(reason, canceledBy);
        Order saved = orderRepository.save(order);
        log.debug("Pedido salvo com status CANCELADO");
        if (stockWasDeducted) {
            log.debug("Restaurando estoque para {} itens", saved.getItems().size());
            try {
                stockService.restoreForOrder(saved.getId(), saved.getItems());
                log.debug("Estoque restaurado com sucesso");
            } catch (Exception e) {
                log.error("Falha ao restaurar estoque para pedido {}: {}", saved.getId(), e.getMessage(), e);
                throw e;
            }
        }
        unlockMenuByOrder(saved.getId());
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(saved.getId()));
        return toOutput(saved);
    }

    private void publishKdsSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception ignored) {
        }
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
                order.getPaymentMethod(),
                order.getTableNumber(),
                order.getCancelReason(),
                order.getCanceledBy(),
                order.getCanceledAt(),
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
                ).toList(),
                item.getStatus().name(),
                item.getCancelReason()
        );
    }

    private Order transition(UUID id, Consumer<Order> action) {
        Order order = orderRepository.findById(id);
        action.accept(order);
        return orderRepository.save(order);
    }
}
