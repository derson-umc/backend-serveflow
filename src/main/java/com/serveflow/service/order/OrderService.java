package com.serveflow.service.order;

import com.serveflow.service.cashier.CashierEventPublisher;
import com.serveflow.service.kds.KdsEventPublisher;
import com.serveflow.service.kds.KdsMapper;
import com.serveflow.dto.order.request.*;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.events.OrderCompletedEvent;
import com.serveflow.integration.AddressResolver;
import com.serveflow.model.address.Address;
import com.serveflow.model.order.*;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.order.OrderRepository;
import com.serveflow.service.stock.StockService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class OrderService {

    private final OrderRepository           orderRepository;
    private final AddressResolver           addressResolver;
    private final StockService              stockService;
    private final MenuRepository            menuRepository;
    private final KdsEventPublisher         kdsEventPublisher;
    private final CashierEventPublisher     cashierEventPublisher;
    private final ApplicationEventPublisher eventPublisher;
    private final KdsMapper                 kdsMapper;

    public OrderService(OrderRepository orderRepository,
                        AddressResolver addressResolver,
                        StockService stockService,
                        MenuRepository menuRepository,
                        @Lazy KdsEventPublisher kdsEventPublisher,
                        CashierEventPublisher cashierEventPublisher,
                        ApplicationEventPublisher eventPublisher,
                        KdsMapper kdsMapper) {
        this.orderRepository      = orderRepository;
        this.addressResolver      = addressResolver;
        this.stockService         = stockService;
        this.menuRepository       = menuRepository;
        this.kdsEventPublisher    = kdsEventPublisher;
        this.cashierEventPublisher = cashierEventPublisher;
        this.eventPublisher       = eventPublisher;
        this.kdsMapper            = kdsMapper;
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

        Order saved = orderRepository.save(order);
        OrderOutput output = toOutput(saved);
        if (hasKitchenItems(saved)) {
            publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        }
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
    public OrderOutput cancelItem(UUID orderId, UUID itemId, String reason) {
        Order order = orderRepository.findById(orderId);
        order.cancelItem(itemId, reason);
        Order saved = orderRepository.save(order);
        OrderOutput output = toOutput(saved);
        publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        return output;
    }

    @Transactional
    public OrderOutput appendItems(UUID id, List<OrderItemInput> itemInputs) {
        if (itemInputs == null || itemInputs.isEmpty())
            throw new IllegalArgumentException("Informe ao menos um item para adicionar.");
        Order order = orderRepository.findById(id);
        if (order.getStatus() == OrderStatus.EM_PREPARO) {
            boolean allDrinks = itemInputs.stream()
                    .allMatch(i -> BEVERAGE_CATEGORIES.contains(i.productCategory()));
            if (!allDrinks)
                throw new IllegalArgumentException("Apenas bebidas podem ser adicionadas a pedidos em preparo.");
        }
        List<OrderItem> newItems = toItems(itemInputs);
        order.appendItems(newItems);
        Order saved = orderRepository.save(order);
        OrderOutput output = toOutput(saved);
        boolean hasKitchenInBatch = newItems.stream()
                .anyMatch(i -> i.getProductCategory() == null
                        || !BEVERAGE_CATEGORIES.contains(i.getProductCategory()));
        if (hasKitchenInBatch) {
            publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        }
        return output;
    }

    @Transactional
    public OrderOutput updateItems(UUID id, List<OrderItemInput> itemInputs) {
        if (itemInputs == null || itemInputs.isEmpty())
            throw new IllegalArgumentException("O pedido deve conter ao menos um item.");
        Order order = orderRepository.findById(id);
        List<OrderItem> newItems = toItems(itemInputs);
        stockService.validateRecipesForOrder(newItems);
        order.replaceItems(newItems);
        Order saved = orderRepository.save(order);
        OrderOutput output = toOutput(saved);
        if (hasKitchenItems(saved)) {
            publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        }
        return output;
    }

    @Transactional
    public OrderOutput confirm(UUID id) {
        Order order = orderRepository.findById(id);
        stockService.validateStockForOrder(order.getItems());
        order.confirm();
        Order saved = orderRepository.save(order);
        stockService.deductForOrder(saved.getId(), saved.getItems());
        OrderOutput output = toOutput(saved);
        if (hasKitchenItems(saved)) {
            publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        }
        return output;
    }

    @Transactional
    public OrderOutput startPreparation(UUID id) {
        Order order = orderRepository.findById(id);
        if (order.getStatus() == OrderStatus.PENDENTE) {
            stockService.validateRecipesForOrder(order.getItems());
            stockService.validateStockForOrder(order.getItems());
            order.confirm();
            order = orderRepository.save(order);
            stockService.deductForOrder(order.getId(), order.getItems());
        }
        order.startPreparation();
        Order saved = orderRepository.save(order);
        OrderOutput output = toOutput(saved);
        if (hasKitchenItems(saved)) {
            publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        }
        return output;
    }

    @Transactional
    public OrderOutput requestPayment(UUID id) {
        Order order = orderRepository.findById(id);

        if (order.getStatus() == OrderStatus.PENDENTE) {
            stockService.validateRecipesForOrder(order.getItems());
            stockService.validateStockForOrder(order.getItems());
            order.confirm();
            order = orderRepository.save(order);
            stockService.deductForOrder(order.getId(), order.getItems());
        }

        order.requestPayment();
        Order saved = orderRepository.save(order);
        OrderOutput output = toOutput(saved);
        String tableId = saved.getTableNumber() != null
                ? "Mesa " + saved.getTableNumber()
                : saved.getCustomerName();
        publishKdsSafely(() ->
                cashierEventPublisher.publishBillCloseRequested(saved.getId(), tableId, saved.getTotal()));
        return output;
    }

    @Transactional
    public OrderOutput markReady(UUID id) {
        OrderOutput output = toOutput(transition(id, Order::markReady));
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(output.id(), output.status()));
        return output;
    }

    @Transactional
    public OrderOutput sendForDelivery(UUID id) {
        OrderOutput output = toOutput(transition(id, Order::sendForDelivery));
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(output.id(), output.status()));
        return output;
    }

    @Transactional
    public OrderOutput complete(UUID id) {
        Order saved = transition(id, Order::complete);
        unlockMenuByOrder(saved.getId());
        OrderOutput output = toOutput(saved);
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(output.id(), output.status()));
        eventPublisher.publishEvent(new OrderCompletedEvent(
                saved.getId(),
                saved.getCustomerName(),
                saved.getType().name(),
                saved.getPaymentMethod(),
                saved.getTotal(),
                null
        ));
        return output;
    }

    @Transactional
    public OrderOutput settleFromCashier(UUID id, String paymentMethod, String settledBy) {
        Order order = orderRepository.findById(id);
        order.registerPayment(paymentMethod);
        order.complete();
        order.closeComanda();
        Order saved = orderRepository.save(order);
        unlockMenuByOrder(saved.getId());
        OrderOutput output = toOutput(saved);
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(output.id(), output.status()));
        eventPublisher.publishEvent(new OrderCompletedEvent(
                saved.getId(),
                saved.getCustomerName(),
                saved.getType().name(),
                saved.getPaymentMethod(),
                saved.getTotal(),
                settledBy
        ));
        return output;
    }

    @Transactional
    public OrderOutput cancel(UUID id, String reason, String canceledBy) {
        Order order = orderRepository.findById(id);
        boolean preparationStarted = order.getStatus() == OrderStatus.EM_PREPARO;
        order.cancel(reason, canceledBy);
        Order saved = orderRepository.save(order);
        if (preparationStarted) {
            stockService.recordLossForOrder(saved.getId(), saved.getItems(), reason);
        }
        unlockMenuByOrder(saved.getId());
        OrderOutput cancelOutput = toOutput(saved);
        publishKdsSafely(() -> kdsEventPublisher.publishRemove(cancelOutput.id(), cancelOutput.status()));
        return cancelOutput;
    }

    private void publishKdsSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception ignored) {
        }
    }

    private static final Set<String> BEVERAGE_CATEGORIES =
            Set.of("BEBIDA_ALCOOLICA", "BEBIDA_NAO_ALCOOLICA");

    private boolean hasKitchenItems(Order order) {
        return order.getItems().stream()
                .anyMatch(item -> item.getProductCategory() == null
                        || !BEVERAGE_CATEGORIES.contains(item.getProductCategory()));
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
        if (dto.productId() == null)
            throw new IllegalArgumentException("ID do produto é obrigatório para o item: " + dto.productName());
        if (dto.unitPrice() == null || dto.unitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço unitário inválido para o item: " + dto.productName());
        List<ItemAdditional> additionals = Optional.ofNullable(dto.additionals())
                .orElse(List.of()).stream()
                .map(a -> new ItemAdditional(a.name(), a.quantity(), a.unitPrice()))
                .toList();
        return new OrderItem(dto.productId(), dto.productName(), dto.quantity(),
                dto.unitPrice(), dto.observation(), additionals, dto.productCategory());
    }

    private OrderOutput toOutput(Order order) {
        return new OrderOutput(
                order.getId(),
                order.getCustomerName(),
                OrderOutput.AddressOutput.from(order.getAddress()),
                order.getType().name(),
                order.getStatus().name(),
                order.getComandaStatus().name(),
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
                item.getCancelReason(),
                item.getProductCategory()
        );
    }

    private Order transition(UUID id, Consumer<Order> action) {
        Order order = orderRepository.findById(id);
        action.accept(order);
        return orderRepository.save(order);
    }
}
