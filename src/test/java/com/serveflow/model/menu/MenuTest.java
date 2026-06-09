package com.serveflow.model.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuTest {

    private static MenuItem menuItem(String name) {
        return MenuItem.create(UUID.randomUUID(), name, "desc", new BigDecimal("10.00"));
    }

    private static Menu openMenu() {
        return Menu.create("Cardápio Almoço",
                new ArrayList<>(List.of(menuItem("Frango"))),
                DayOfWeek.MONDAY, MenuShift.AFTERNOON);
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("cria menu com status OPEN e id gerado")
        void create_setsOpenStatusAndGeneratesId() {
            Menu menu = Menu.create("Jantar", List.of(), DayOfWeek.FRIDAY, MenuShift.EVENING);
            assertThat(menu.getId()).isNotNull();
            assertThat(menu.getStatus()).isEqualTo(MenuStatus.OPEN);
            assertThat(menu.getName()).isEqualTo("Jantar");
            assertThat(menu.getActiveOrderId()).isNull();
            assertThat(menu.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
            assertThat(menu.getShift()).isEqualTo(MenuShift.EVENING);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando nome em branco")
        void create_throwsWhenNameBlank() {
            assertThatThrownBy(() -> Menu.create("  ", List.of(), DayOfWeek.MONDAY, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do menu");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor direto")
    class Constructor {

        @Test
        @DisplayName("lança NullPointerException quando id ausente")
        void constructor_throwsWhenIdNull() {
            assertThatThrownBy(() -> new Menu(null, "X", MenuStatus.OPEN, null,
                    List.of(), LocalDateTime.now(), null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança NullPointerException quando status ausente")
        void constructor_throwsWhenStatusNull() {
            assertThatThrownBy(() -> new Menu(UUID.randomUUID(), "X", null, null,
                    List.of(), LocalDateTime.now(), null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando nome nulo")
        void constructor_throwsWhenNameNull() {
            assertThatThrownBy(() -> new Menu(UUID.randomUUID(), null, MenuStatus.OPEN, null,
                    List.of(), LocalDateTime.now(), null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("lock()")
    class Lock {

        @Test
        @DisplayName("trava menu OPEN com orderId e muda status para LOCKED")
        void lock_success() {
            Menu menu = openMenu();
            UUID orderId = UUID.randomUUID();
            menu.lock(orderId);
            assertThat(menu.isLocked()).isTrue();
            assertThat(menu.getActiveOrderId()).isEqualTo(orderId);
            assertThat(menu.getStatus()).isEqualTo(MenuStatus.LOCKED);
        }

        @Test
        @DisplayName("lança IllegalStateException quando menu já está LOCKED")
        void lock_throwsWhenAlreadyLocked() {
            Menu menu = openMenu();
            UUID orderId = UUID.randomUUID();
            menu.lock(orderId);
            assertThatThrownBy(() -> menu.lock(UUID.randomUUID()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("já está travado");
        }

        @Test
        @DisplayName("lança NullPointerException quando orderId nulo")
        void lock_throwsWhenOrderIdNull() {
            Menu menu = openMenu();
            assertThatThrownBy(() -> menu.lock(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("unlock()")
    class Unlock {

        @Test
        @DisplayName("libera menu LOCKED de volta para OPEN")
        void unlock_success() {
            Menu menu = openMenu();
            menu.lock(UUID.randomUUID());
            menu.unlock();
            assertThat(menu.isOpen()).isTrue();
            assertThat(menu.getActiveOrderId()).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("addItem()")
    class AddItem {

        @Test
        @DisplayName("adiciona item ao menu")
        void addItem_success() {
            Menu menu = openMenu();
            int before = menu.getItems().size();
            menu.addItem(menuItem("Arroz"));
            assertThat(menu.getItems()).hasSize(before + 1);
        }

        @Test
        @DisplayName("lança NullPointerException quando item nulo")
        void addItem_throwsWhenNull() {
            Menu menu = openMenu();
            assertThatThrownBy(() -> menu.addItem(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findItem()")
    class FindItem {

        @Test
        @DisplayName("retorna item quando encontrado pelo id")
        void findItem_found() {
            Menu menu = openMenu();
            UUID itemId = menu.getItems().get(0).getId();
            MenuItem found = menu.findItem(itemId);
            assertThat(found.getId()).isEqualTo(itemId);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando item não encontrado")
        void findItem_throwsWhenNotFound() {
            Menu menu = openMenu();
            assertThatThrownBy(() -> menu.findItem(UUID.randomUUID()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("markItemAsRemoved()")
    class MarkItemAsRemoved {

        @Test
        @DisplayName("marca item como removido com chef informado")
        void markItemAsRemoved_success() {
            Menu menu = openMenu();
            UUID itemId = menu.getItems().get(0).getId();
            menu.markItemAsRemoved(itemId, "Chef João");
            MenuItem item = menu.findItem(itemId);
            assertThat(item.isRemoved()).isTrue();
            assertThat(item.getRemovedBy()).isEqualTo("Chef João");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAvailableItems()")
    class GetAvailableItems {

        @Test
        @DisplayName("retorna apenas itens disponíveis e não removidos")
        void getAvailableItems_filtersCorrectly() {
            Menu menu = openMenu();
            MenuItem available = menuItem("Disponível");
            MenuItem unavailable = menuItem("Indisponível");
            unavailable.updateAvailability(false);
            MenuItem removed = menuItem("Removido");

            menu.addItem(available);
            menu.addItem(unavailable);
            menu.addItem(removed);

            // Remove the last item (Removido)
            UUID removedId = menu.getItems().stream()
                    .filter(i -> i.getName().equals("Removido"))
                    .findFirst().get().getId();
            menu.markItemAsRemoved(removedId, "Chef");

            List<MenuItem> result = menu.getAvailableItems();
            assertThat(result).extracting(MenuItem::getName)
                    .contains("Frango", "Disponível")
                    .doesNotContain("Indisponível", "Removido");
        }

        @Test
        @DisplayName("retorna lista vazia quando todos os itens removidos")
        void getAvailableItems_emptyWhenAllRemoved() {
            Menu menu = openMenu();
            UUID id = menu.getItems().get(0).getId();
            menu.markItemAsRemoved(id, "Chef");
            assertThat(menu.getAvailableItems()).isEmpty();
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("dois menus com mesmo id são iguais")
        void equals_sameId() {
            UUID id = UUID.randomUUID();
            Menu m1 = new Menu(id, "A", MenuStatus.OPEN, null, List.of(), LocalDateTime.now(), null, null, null);
            Menu m2 = new Menu(id, "B", MenuStatus.LOCKED, null, List.of(), LocalDateTime.now(), null, null, null);
            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("dois menus com ids diferentes não são iguais")
        void equals_differentId() {
            Menu m1 = openMenu();
            Menu m2 = openMenu();
            assertThat(m1).isNotEqualTo(m2);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getItems() — lista imutável")
    class GetItemsImmutable {

        @Test
        @DisplayName("getItems() retorna cópia imutável")
        void getItems_isUnmodifiable() {
            Menu menu = openMenu();
            assertThatThrownBy(() -> menu.getItems().add(menuItem("X")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
