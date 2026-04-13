package com.serveflow.application.usecase;

import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.model.menu.Menu;
import com.serveflow.domain.model.menu.MenuItem;
import com.serveflow.domain.model.order.Order;
import com.serveflow.domain.model.order.OrderItem;
import com.serveflow.domain.model.order.OrderType;
import com.serveflow.domain.repository.MenuRepository;
import com.serveflow.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StartOrder {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;

    public StartOrder(MenuRepository menuRepository, OrderRepository orderRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order execute(UUID menuId, String customerName, OrderType type,
                         Address address, String observation,
                         List<MenuItemSelection> selections) {

        Menu menu = menuRepository.findById(menuId);

        if (menu.isLocked()) {
            throw new IllegalStateException(
                    "Menu esta travado. Utilize o endpoint de adição avulsa ao pedido. " + menu.getActiveOrderId() + ".");
        }

        List<OrderItem> items = selections.stream().map(selection -> {
            MenuItem menuItem = menu.findItem(selection.menuItemId());

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException(
                        "Item '" + menuItem.getName() + "' não esta disponível no momento.");
            }

            return new OrderItem(
                    menuItem.getProductId(),
                    menuItem.getName(),
                    selection.quantity(),
                    menuItem.getPrice(),
                    selection.observation(),
                    List.of()
            );
        }).toList();

        Order order = Order.create(customerName, address, type, observation);
        items.forEach(order::addItem);

        Order savedOrder = orderRepository.save(order);

        menu.lock(savedOrder.getId());
        menuRepository.save(menu);

        return savedOrder;
    }

    public record MenuItemSelection(UUID menuItemId, int quantity, String observation) {}
}
