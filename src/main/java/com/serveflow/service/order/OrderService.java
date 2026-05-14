package com.serveflow.service.order;

import com.serveflow.dto.order.request.*;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.integration.FindPostalCode;
import com.serveflow.model.address.*;
import com.serveflow.model.address.Number;
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
    private final FindPostalCode findPostalCode;
    private final StockService stockService;
    private final MenuRepository menuRepository;

    public OrderService(OrderRepository orderRepository,
                        FindPostalCode findPostalCode,
                        StockService stockService,
                        MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.findPostalCode = findPostalCode;
        this.stockService = stockService;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public OrderOutput create(OrderInput request) {
        Address resolvedAddress = resolveAddress(request.address());
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

    private Address resolveAddress(AddressInput dto) {
        if (dto == null) return null;

        if (dto.cep() != null && !dto.cep().isBlank()) {
            Optional<Address> resolved = findPostalCode.findByCep(dto.cep());
            if (resolved.isPresent()) {
                Address base = resolved.get();
                String num = dto.number() != null && !dto.number().isBlank() ? dto.number() : "S/N";
                Complement comp = dto.complement() != null && !dto.complement().isBlank()
                        ? new Complement(dto.complement()) : null;
                return Address.create(base.getCep(), base.getStreet(), base.getCity(),
                        base.getState(), new Number(num), comp);
            }
        }

        boolean hasManualFields = dto.street() != null && !dto.street().isBlank()
                && dto.city() != null && !dto.city().isBlank()
                && dto.state() != null && !dto.state().isBlank()
                && dto.number() != null && !dto.number().isBlank();

        if (!hasManualFields) return null;

        return Address.create(
                dto.cep() != null && !dto.cep().isBlank() ? new Cep(dto.cep()) : null,
                new Street(dto.street()),
                new City(dto.city()),
                new State(dto.state()),
                new Number(dto.number()),
                dto.complement() != null && !dto.complement().isBlank() ? new Complement(dto.complement()) : null
        );
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
                toAddressOutput(order.getAddress()),
                order.getType().name(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getObservation(),
                order.getTotal(),
                order.getItems().stream().map(this::toItemOutput).toList()
        );
    }

    private OrderOutput.AddressOutput toAddressOutput(Address a) {
        if (a == null) return null;
        return new OrderOutput.AddressOutput(
                a.getId(),
                a.getCep() != null ? a.getCep().getValue() : null,
                a.getStreet() != null ? a.getStreet().getValue() : null,
                a.getCity() != null ? a.getCity().getValue() : null,
                a.getState() != null ? a.getState().getValue().name() : null,
                a.getNumber() != null ? a.getNumber().getValue() : null,
                a.getComplement() != null ? a.getComplement().getValue() : null
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
