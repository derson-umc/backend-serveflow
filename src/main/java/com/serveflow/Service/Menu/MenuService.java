package com.serveflow.Service.Menu;

import com.serveflow.Dto.Menu.Request.MenuInput;
import com.serveflow.Dto.Menu.Request.PlaceOrderInput;
import com.serveflow.Dto.Menu.Request.RemoveMenuItemInput;
import com.serveflow.Dto.Menu.Response.MenuOutput;
import com.serveflow.Dto.Order.Request.AddressInput;
import com.serveflow.Dto.Order.Response.OrderOutput;
import com.serveflow.Integration.FindPostalCode;
import com.serveflow.Model.Menu.Menu;
import com.serveflow.Model.Menu.MenuItem;
import com.serveflow.Model.Order.*;
import com.serveflow.Model.Order.Number;
import com.serveflow.Repository.Menu.MenuRepository;
import com.serveflow.Repository.Order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final FindPostalCode findPostalCode;

    public MenuService(MenuRepository menuRepository,
                       OrderRepository orderRepository,
                       FindPostalCode findPostalCode) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.findPostalCode = findPostalCode;
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
        Address resolvedAddress = resolveAddress(request.address());

        Order order = Order.create(request.customerName(), resolvedAddress, orderType, request.observation());
        items.forEach(order::addItem);

        Order saved = orderRepository.save(order);

        menu.lock(saved.getId());
        menuRepository.save(menu);

        return toOrderOutput(saved);
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
                toAddressOutput(order.getAddress()),
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
}
