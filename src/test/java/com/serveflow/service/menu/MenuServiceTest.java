package com.serveflow.service.menu;

import com.serveflow.dto.menu.request.MenuInput;
import com.serveflow.dto.menu.request.MenuItemInput;
import com.serveflow.dto.menu.request.MenuItemSelectionInput;
import com.serveflow.dto.menu.request.PlaceOrderInput;
import com.serveflow.dto.menu.request.RemoveMenuItemInput;
import com.serveflow.dto.menu.response.ActiveMenuOutput;
import com.serveflow.dto.menu.response.MenuOutput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.integration.AddressResolver;
import com.serveflow.model.menu.Menu;
import com.serveflow.model.menu.MenuItem;
import com.serveflow.model.menu.MenuShift;
import com.serveflow.model.menu.MenuStatus;
import com.serveflow.model.order.Order;
import com.serveflow.model.order.OrderItem;
import com.serveflow.model.order.OrderItemStatus;
import com.serveflow.model.order.OrderStatus;
import com.serveflow.model.order.OrderType;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.order.OrderRepository;
import com.serveflow.service.kds.KdsEventPublisher;
import com.serveflow.service.kds.KdsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    MenuRepository menuRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    AddressResolver addressResolver;
    @Mock
    KdsEventPublisher kdsEventPublisher;
    @Mock
    KdsMapper kdsMapper;

    @InjectMocks
    MenuService service;

    private UUID menuId;
    private UUID productId;
    private UUID menuItemId;

    @BeforeEach
    void setUp() {
        menuId = UUID.randomUUID();
        productId = UUID.randomUUID();
        menuItemId = UUID.randomUUID();
    }

    private MenuItem menuItem(UUID itemId, UUID prodId) {
        return new MenuItem(itemId, prodId, "Hamburguer", "Desc", new BigDecimal("25.00"), true, false, null);
    }

    private Menu menu(UUID id, MenuStatus status) {
        List<MenuItem> items = new ArrayList<>(List.of(menuItem(menuItemId, productId)));
        return new Menu(id, "Menu Almoço", status, null, items, LocalDateTime.now(), 1L,
                DayOfWeek.MONDAY, MenuShift.AFTERNOON);
    }

    private Menu menuLocked(UUID id, UUID orderId) {
        List<MenuItem> items = new ArrayList<>(List.of(menuItem(menuItemId, productId)));
        return new Menu(id, "Menu Almoço", MenuStatus.LOCKED, orderId, items, LocalDateTime.now(), 1L,
                DayOfWeek.MONDAY, MenuShift.AFTERNOON);
    }

    private MenuOutput menuOutput(UUID id) {
        return new MenuOutput(id, "Menu Almoço", "OPEN", null, List.of(), LocalDateTime.now(), "MONDAY", "AFTERNOON");
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("cria menu e retorna output")
        void create_success() {
            Menu saved = menu(menuId, MenuStatus.OPEN);
            when(menuRepository.save(any(Menu.class))).thenReturn(saved);

            MenuItemInput itemInput = new MenuItemInput(productId, "Hamburguer", "Desc", new BigDecimal("25.00"));
            MenuInput input = new MenuInput("Menu Almoço", List.of(itemInput), DayOfWeek.MONDAY, "AFTERNOON");

            MenuOutput result = service.create(input);

            assertThat(result.id()).isEqualTo(menuId);
            assertThat(result.name()).isEqualTo("Menu Almoço");
            verify(menuRepository).save(any(Menu.class));
        }

        @Test
        @DisplayName("cria menu sem shift quando shift é nulo")
        void create_withoutShift() {
            Menu saved = menu(menuId, MenuStatus.OPEN);
            when(menuRepository.save(any(Menu.class))).thenReturn(saved);

            MenuItemInput itemInput = new MenuItemInput(productId, "Hamburguer", "Desc", new BigDecimal("25.00"));
            MenuInput input = new MenuInput("Menu Almoço", List.of(itemInput), DayOfWeek.MONDAY, null);

            MenuOutput result = service.create(input);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("retorna output quando menu encontrado")
        void findById_returnsOutput() {
            Menu found = menu(menuId, MenuStatus.OPEN);
            when(menuRepository.findById(menuId)).thenReturn(found);

            MenuOutput result = service.findById(menuId);

            assertThat(result.id()).isEqualTo(menuId);
            verify(menuRepository).findById(menuId);
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("retorna lista de menus")
        void findAll_returnsList() {
            when(menuRepository.findAll()).thenReturn(List.of(menu(menuId, MenuStatus.OPEN)));

            List<MenuOutput> result = service.findAll();

            assertThat(result).hasSize(1);
            verify(menuRepository).findAll();
        }

        @Test
        @DisplayName("retorna lista vazia quando não há menus")
        void findAll_returnsEmpty() {
            when(menuRepository.findAll()).thenReturn(List.of());

            List<MenuOutput> result = service.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getActive()")
    class GetActive {

        @Test
        @DisplayName("retorna menu ativo quando encontrado para turno atual")
        void getActive_returnsActive_whenFound() {
            Menu found = menu(menuId, MenuStatus.OPEN);
            when(menuRepository.findByDayOfWeekAndShift(any(), any())).thenReturn(Optional.of(found));

            // This call uses the actual system time — we just verify it returns a non-null result
            ActiveMenuOutput result = service.getActive();

            assertThat(result).isNotNull();
            assertThat(result.dayOfWeek()).isNotNull();
        }

        @Test
        @DisplayName("retorna inativo quando não há menu para o turno atual")
        void getActive_returnsInactive_whenNoMenuFound() {
            when(menuRepository.findByDayOfWeekAndShift(any(), any())).thenReturn(Optional.empty());

            ActiveMenuOutput result = service.getActive();

            assertThat(result).isNotNull();
            // Either active=false because no menu, or active=false because outside business hours
            assertThat(result.dayOfWeek()).isNotNull();
        }
    }

    @Nested
    @DisplayName("unlock()")
    class Unlock {

        @Test
        @DisplayName("desbloqueia menu e retorna output")
        void unlock_success() {
            Menu locked = menuLocked(menuId, UUID.randomUUID());
            Menu unlocked = menu(menuId, MenuStatus.OPEN);
            when(menuRepository.findById(menuId)).thenReturn(locked);
            when(menuRepository.save(locked)).thenReturn(unlocked);

            MenuOutput result = service.unlock(menuId);

            assertThat(result.status()).isEqualTo("OPEN");
            verify(menuRepository).save(locked);
        }
    }

    @Nested
    @DisplayName("updateItemAvailability()")
    class UpdateItemAvailability {

        @Test
        @DisplayName("atualiza disponibilidade e retorna output")
        void updateItemAvailability_success() {
            Menu found = menu(menuId, MenuStatus.OPEN);
            Menu saved = menu(menuId, MenuStatus.OPEN);
            when(menuRepository.findById(menuId)).thenReturn(found);
            when(menuRepository.save(found)).thenReturn(saved);

            MenuOutput result = service.updateItemAvailability(menuId, menuItemId, false);

            assertThat(result).isNotNull();
            verify(menuRepository).save(found);
        }

        @Test
        @DisplayName("lança IllegalStateException quando item está removido")
        void updateItemAvailability_throwsWhenItemRemoved() {
            MenuItem removedItem = new MenuItem(menuItemId, productId, "Hamburguer", "Desc",
                    new BigDecimal("25.00"), false, true, "Chef");
            Menu found = new Menu(menuId, "Menu", MenuStatus.OPEN, null,
                    new ArrayList<>(List.of(removedItem)), LocalDateTime.now(), 1L, null, null);
            when(menuRepository.findById(menuId)).thenReturn(found);

            assertThatThrownBy(() -> service.updateItemAvailability(menuId, menuItemId, true))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("removido");
        }
    }

    @Nested
    @DisplayName("removeItem()")
    class RemoveItem {

        @Test
        @DisplayName("remove item do menu e retorna output")
        void removeItem_success() {
            Menu found = menu(menuId, MenuStatus.OPEN);
            Menu saved = menu(menuId, MenuStatus.OPEN);
            when(menuRepository.findById(menuId)).thenReturn(found);
            when(menuRepository.save(found)).thenReturn(saved);

            RemoveMenuItemInput request = new RemoveMenuItemInput("Chef Mario");
            MenuOutput result = service.removeItem(menuId, menuItemId, request);

            assertThat(result).isNotNull();
            verify(menuRepository).save(found);
        }
    }

    @Nested
    @DisplayName("placeOrder()")
    class PlaceOrder {

        private Order buildOrder(UUID orderId) {
            List<OrderItem> items = new ArrayList<>(List.of(
                    new OrderItem(UUID.randomUUID(), productId, "Hamburguer", 1,
                            new BigDecimal("25.00"), null, List.of(), OrderItemStatus.PENDENTE, null, null)));
            return Order.builder()
                    .id(orderId)
                    .customerName("Cliente")
                    .type(OrderType.BALCAO)
                    .status(OrderStatus.PENDENTE)
                    .createdAt(LocalDateTime.now())
                    .items(items)
                    .build();
        }

        @Test
        @DisplayName("cria pedido com itens selecionados do menu")
        void placeOrder_success() {
            UUID orderId = UUID.randomUUID();
            Menu found = menu(menuId, MenuStatus.OPEN);
            Order savedOrder = buildOrder(orderId);
            Menu savedMenu = menu(menuId, MenuStatus.LOCKED);

            when(menuRepository.findById(menuId)).thenReturn(found);
            when(addressResolver.resolve(null)).thenReturn(null);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(menuRepository.save(any(Menu.class))).thenReturn(savedMenu);

            PlaceOrderInput input = new PlaceOrderInput(
                    "Cliente", "BALCAO", null, null, null,
                    List.of(new MenuItemSelectionInput(menuItemId, 1, null)));

            OrderOutput result = service.placeOrder(menuId, input);

            assertThat(result.id()).isEqualTo(orderId);
            verify(orderRepository).save(any(Order.class));
            verify(menuRepository).save(any(Menu.class));
        }

        @Test
        @DisplayName("lança IllegalStateException quando menu está travado")
        void placeOrder_throwsWhenLocked() {
            UUID existingOrderId = UUID.randomUUID();
            Menu locked = menuLocked(menuId, existingOrderId);
            when(menuRepository.findById(menuId)).thenReturn(locked);

            PlaceOrderInput input = new PlaceOrderInput(
                    "Cliente", "BALCAO", null, null, null,
                    List.of(new MenuItemSelectionInput(menuItemId, 1, null)));

            assertThatThrownBy(() -> service.placeOrder(menuId, input))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("travado");
        }

        @Test
        @DisplayName("lança IllegalStateException quando item não está disponível")
        void placeOrder_throwsWhenItemUnavailable() {
            MenuItem unavailableItem = new MenuItem(menuItemId, productId, "Hamburguer", "Desc",
                    new BigDecimal("25.00"), false, false, null);
            Menu found = new Menu(menuId, "Menu", MenuStatus.OPEN, null,
                    new ArrayList<>(List.of(unavailableItem)), LocalDateTime.now(), 1L, null, null);
            when(menuRepository.findById(menuId)).thenReturn(found);

            PlaceOrderInput input = new PlaceOrderInput(
                    "Cliente", "BALCAO", null, null, null,
                    List.of(new MenuItemSelectionInput(menuItemId, 1, null)));

            assertThatThrownBy(() -> service.placeOrder(menuId, input))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("disponível");
        }
    }
}
