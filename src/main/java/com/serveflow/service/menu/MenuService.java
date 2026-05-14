package com.serveflow.service.menu;

import com.serveflow.dto.menu.request.MenuInput;
import com.serveflow.dto.menu.request.PlaceOrderInput;
import com.serveflow.dto.menu.request.RemoveMenuItemInput;
import com.serveflow.dto.menu.response.MenuOutput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.integration.AddressResolver;
import com.serveflow.model.address.Address;
import com.serveflow.model.menu.Menu;
import com.serveflow.model.menu.MenuItem;
import com.serveflow.model.order.*;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final AddressResolver addressResolver;

    public MenuService(MenuRepository menuRepository,
                       OrderRepository orderRepository,
                       AddressResolver addressResolver) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.addressResolver = addressResolver;
    }

    @Transactional
    public MenuOutput create(MenuInput request) {
        List<MenuItem> items = request.items().stream()
                .map(i -> MenuItem.create(i.productId(), i.name(), i.description(), i.price()))
                .toList();
        return toOutput(menuRepository.save(Menu.create(request.name(), items)));
    }

    public MenuOutput findById(UUID id) {
        return toOutput(menuRepository.findById(id));
    }

    public List<MenuOutput> findAll() {
        return menuRepository.findAll().stream().map(this::toOutput).toList();
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
    public OrderOutput placeOrder(UUID menuId, PlaceOrderInput request) {
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

        Order order = Order.create(request.customerName(), resolvedAddress, orderType, request.observation());
        items.forEach(order::addItem);

        Order saved = orderRepository.save(order);

        menu.lock(saved.getId());
        menuRepository.save(menu);

        return toOrderOutput(saved);
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
                menu.getCreatedAt()
        );
    }

    private OrderOutput toOrderOutput(Order order) {
        return new OrderOutput(
                order.getId(),
                order.getCustomerName(),
                OrderOutput.AddressOutput.from(order.getAddress()),
                order.getType().name(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getObservation(),
                order.getTotal(),
                order.getItems().stream().map(item ->
                        new OrderOutput.OrderItemOutput(
                                item.getId(), item.getProductId(), item.getProductName(),
                                item.getQuantity(), item.getUnitPrice(), item.getObservation(),
                                item.getTotal(), List.of())
                ).toList()
        );
    }

}
