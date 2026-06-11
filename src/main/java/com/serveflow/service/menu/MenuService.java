package com.serveflow.service.menu;

import com.serveflow.service.kds.KdsEventPublisher;
import com.serveflow.service.kds.KdsMapper;
import com.serveflow.dto.menu.request.MenuInput;
import com.serveflow.dto.menu.request.PlaceOrderInput;
import com.serveflow.dto.menu.request.RemoveMenuItemInput;
import com.serveflow.dto.menu.response.ActiveMenuOutput;
import com.serveflow.dto.menu.response.MenuOutput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.integration.AddressResolver;
import com.serveflow.model.address.Address;
import com.serveflow.model.menu.Menu;
import com.serveflow.model.menu.MenuItem;
import com.serveflow.model.menu.MenuShift;
import com.serveflow.model.order.*;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.order.OrderRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final AddressResolver addressResolver;
    private final KdsEventPublisher kdsEventPublisher;
    private final KdsMapper kdsMapper;

    public MenuService(MenuRepository menuRepository,
                       OrderRepository orderRepository,
                       AddressResolver addressResolver,
                       @Lazy KdsEventPublisher kdsEventPublisher,
                       KdsMapper kdsMapper) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.addressResolver = addressResolver;
        this.kdsEventPublisher = kdsEventPublisher;
        this.kdsMapper = kdsMapper;
    }

    @Transactional
    public MenuOutput create(MenuInput request) {
        List<MenuItem> items = request.items().stream()
                .map(i -> MenuItem.create(i.productId(), i.name(), i.description(), i.price()))
                .toList();
        MenuShift shift = request.shift() != null ? MenuShift.valueOf(request.shift().toUpperCase()) : null;
        return toOutput(menuRepository.save(Menu.create(request.name(), items, request.dayOfWeek(), shift)));
    }

    public MenuOutput findById(UUID id) {
        return toOutput(menuRepository.findById(id));
    }

    public List<MenuOutput> findAll() {
        return menuRepository.findAll().stream().map(this::toOutput).toList();
    }

    public ActiveMenuOutput getActive() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        DayOfWeek today = now.getDayOfWeek();
        Optional<MenuShift> currentShift = resolveShift(now.toLocalTime());

        if (currentShift.isEmpty()) {
            return new ActiveMenuOutput(false, today.name(), null, null);
        }

        MenuShift shift = currentShift.get();
        return menuRepository.findByDayOfWeekAndShift(today, shift)
                .map(menu -> new ActiveMenuOutput(true, today.name(), shift.name(), toOutput(menu)))
                .orElse(new ActiveMenuOutput(false, today.name(), shift.name(), null));
    }

    private Optional<MenuShift> resolveShift(LocalTime time) {
        if (!time.isBefore(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(12, 0))) {
            return Optional.of(MenuShift.MORNING);
        }
        if (!time.isBefore(LocalTime.of(12, 0)) && time.isBefore(LocalTime.of(18, 0))) {
            return Optional.of(MenuShift.AFTERNOON);
        }
        if (!time.isBefore(LocalTime.of(18, 0))) {
            return Optional.of(MenuShift.EVENING);
        }
        return Optional.empty();
    }

    @Transactional
    public MenuOutput unlock(UUID id) {
        Menu menu = menuRepository.findById(id);
        menu.unlock();
        return toOutput(menuRepository.save(menu));
    }

    @Transactional
    public MenuOutput updateItemAvailability(UUID menuId, UUID menuItemId, boolean available) {
        Menu menu = menuRepository.findById(menuId);
        MenuItem item = menu.findItem(menuItemId);
        if (item.isRemoved())
            throw new IllegalStateException("Não é possível alterar a disponibilidade de um item já removido do menu.");
        item.updateAvailability(available);
        return toOutput(menuRepository.save(menu));
    }

    @Transactional
    public MenuOutput removeItem(UUID menuId, UUID menuItemId, RemoveMenuItemInput request) {
        Menu menu = menuRepository.findById(menuId);
        menu.markItemAsRemoved(menuItemId, request.chefName());
        return toOutput(menuRepository.save(menu));
    }

    @Transactional
    public OrderOutput placeOrder(UUID menuId, PlaceOrderInput request, String createdBy) {
        Menu menu = menuRepository.findById(menuId);

        if (menu.isLocked()) {
            throw new IllegalStateException(
                    "Menu está travado. Utilize a adição avulsa ao pedido: " + menu.getActiveOrderId() + ".");
        }

        List<OrderItem> items = request.selections().stream().map(selection -> {
            MenuItem menuItem = menu.findItem(selection.menuItemId());
            if (!menuItem.isAvailable() || menuItem.isRemoved())
                throw new IllegalStateException("Item '" + menuItem.getName() + "' não está disponível no momento.");
            return new OrderItem(
                    menuItem.getProductId(),
                    menuItem.getName(),
                    selection.quantity(),
                    menuItem.getPrice(),
                    selection.observation(),
                    List.of()
            );
        }).toList();

        OrderType orderType = OrderType.valueOf(request.type().toUpperCase());
        Address resolvedAddress = addressResolver.resolve(request.address());

        Order order = Order.create(request.customerName(), resolvedAddress, orderType, request.observation(), request.tableNumber(), createdBy);
        items.forEach(order::addItem);

        Order saved = orderRepository.save(order);

        menu.lock(saved.getId());
        menuRepository.save(menu);

        OrderOutput output = toOrderOutput(saved);
        publishKdsSafely(() -> kdsEventPublisher.publishUpdate(kdsMapper.toOutput(output)));
        return output;
    }

    private void publishKdsSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception ignored) {
        }
    }

    private MenuOutput toOutput(Menu menu) {
        return new MenuOutput(
                menu.getId(),
                menu.getName(),
                menu.getStatus().name(),
                menu.getActiveOrderId(),
                menu.getItems().stream().map(i ->
                        new MenuOutput.MenuItemOutput(
                                i.getId(), i.getProductId(), i.getName(),
                                i.getDescription(), i.getPrice(), i.isAvailable(),
                                i.isRemoved(), i.getRemovedBy())
                ).toList(),
                menu.getCreatedAt(),
                menu.getDayOfWeek() != null ? menu.getDayOfWeek().name() : null,
                menu.getShift() != null ? menu.getShift().name() : null
        );
    }

    private OrderOutput toOrderOutput(Order order) {
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
                order.getCreatedBy(),
                order.getTotal(),
                order.getItems().stream().map(item ->
                        new OrderOutput.OrderItemOutput(
                                item.getId(), item.getProductId(), item.getProductName(),
                                item.getQuantity(), item.getUnitPrice(), item.getObservation(),
                                item.getTotal(), List.of(),
                                item.getStatus().name(), item.getCancelReason(),
                                item.getProductCategory())
                ).toList()
        );
    }

}
