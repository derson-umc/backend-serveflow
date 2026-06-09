package com.serveflow.model.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static OrderItem item(String name) {
        return new OrderItem(
                UUID.randomUUID(), UUID.randomUUID(), name, 1,
                new BigDecimal("10.00"), null, List.of(),
                OrderItemStatus.PENDENTE, null, null);
    }

    private static Order pendingBalcao() {
        return Order.builder()
                .id(UUID.randomUUID())
                .customerName("Cliente Teste")
                .type(OrderType.BALCAO)
                .status(OrderStatus.PENDENTE)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>(List.of(item("Produto A"))))
                .build();
    }

    private static Order orderWithStatus(OrderStatus status) {
        return Order.builder()
                .id(UUID.randomUUID())
                .customerName("Cliente")
                .type(OrderType.BALCAO)
                .status(status)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>(List.of(item("X"))))
                .build();
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("cria pedido BALCAO com status PENDENTE e comanda ABERTA")
        void create_balcao_defaultStatus() {
            Order order = Order.create("João", null, OrderType.BALCAO, null, null);

            assertThat(order.getCustomerName()).isEqualTo("João");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDENTE);
            assertThat(order.getComandaStatus()).isEqualTo(ComandaStatus.ABERTA);
            assertThat(order.getType()).isEqualTo(OrderType.BALCAO);
            assertThat(order.getId()).isNotNull();
            assertThat(order.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("strip em customerName e observation")
        void create_stripsCustomerNameAndObservation() {
            Order order = Order.create("  Maria  ", null, OrderType.BALCAO, "  obs  ", null);
            assertThat(order.getCustomerName()).isEqualTo("Maria");
            assertThat(order.getObservation()).isEqualTo("obs");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando nome está em branco")
        void create_throwsWhenNameBlank() {
            assertThatThrownBy(() -> Order.create("   ", null, OrderType.BALCAO, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do cliente");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando DELIVERY sem endereço")
        void create_throwsWhenDeliveryWithoutAddress() {
            assertThatThrownBy(() -> Order.create("X", null, OrderType.DELIVERY, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Endereço");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando MESA sem tableNumber")
        void create_throwsWhenMesaWithoutTable() {
            assertThatThrownBy(() -> Order.create("X", null, OrderType.MESA, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mesa");
        }

        @Test
        @DisplayName("cria pedido MESA com tableNumber correto")
        void create_mesa_withTableNumber() {
            Order order = Order.create("X", null, OrderType.MESA, null, "5");
            assertThat(order.getTableNumber()).isEqualTo("5");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("addItem()")
    class AddItem {

        @Test
        @DisplayName("adiciona item ao pedido PENDENTE")
        void addItem_success() {
            Order order = pendingBalcao();
            int before = order.getItemCount();
            order.addItem(item("Novo"));
            assertThat(order.getItemCount()).isEqualTo(before + 1);
        }

        @Test
        @DisplayName("lança IllegalStateException quando pedido está ENTREGUE (final)")
        void addItem_throwsWhenFinalized() {
            Order order = orderWithStatus(OrderStatus.ENTREGUE);
            assertThatThrownBy(() -> order.addItem(item("X")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("finalizado");
        }

        @Test
        @DisplayName("lança NullPointerException quando item é nulo")
        void addItem_throwsWhenNull() {
            Order order = pendingBalcao();
            assertThatThrownBy(() -> order.addItem(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("removeItem()")
    class RemoveItem {

        @Test
        @DisplayName("lança IllegalStateException quando tentar remover único item")
        void removeItem_throwsWhenOnlyOneItem() {
            Order order = pendingBalcao();
            OrderItem only = order.getItems().get(0);
            assertThatThrownBy(() -> order.removeItem(only))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ao menos um item");
        }

        @Test
        @DisplayName("remove item quando há mais de um")
        void removeItem_success() {
            Order order = pendingBalcao();
            OrderItem extra = item("Extra");
            order.addItem(extra);
            order.removeItem(extra);
            assertThat(order.getItemCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando item não pertence ao pedido")
        void removeItem_throwsWhenNotFound() {
            Order order = pendingBalcao();
            order.addItem(item("A2"));
            OrderItem stranger = item("Estranho");
            assertThatThrownBy(() -> order.removeItem(stranger))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("confirm()")
    class Confirm {

        @Test
        @DisplayName("confirma pedido PENDENTE e muda status para ENVIADO")
        void confirm_success() {
            Order order = pendingBalcao();
            order.confirm();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.ENVIADO);
        }

        @Test
        @DisplayName("lança IllegalStateException quando não há itens")
        void confirm_throwsWhenNoItems() {
            Order order = Order.builder()
                    .id(UUID.randomUUID())
                    .customerName("X")
                    .type(OrderType.BALCAO)
                    .status(OrderStatus.PENDENTE)
                    .createdAt(LocalDateTime.now())
                    .items(new ArrayList<>())
                    .build();
            assertThatThrownBy(order::confirm)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ao menos um item");
        }

        @Test
        @DisplayName("lança IllegalStateException quando transição inválida (CANCELADO -> ENVIADO)")
        void confirm_throwsOnInvalidTransition() {
            Order order = orderWithStatus(OrderStatus.CANCELADO);
            assertThatThrownBy(order::confirm)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição inválida");
        }

        @Test
        @DisplayName("sincroniza status dos itens para ENVIADO")
        void confirm_syncsItemStatus() {
            Order order = pendingBalcao();
            order.confirm();
            order.getItems().forEach(i ->
                    assertThat(i.getStatus()).isEqualTo(OrderItemStatus.ENVIADO));
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("startPreparation()")
    class StartPreparation {

        @Test
        @DisplayName("muda status de ENVIADO para EM_PREPARO")
        void startPreparation_success() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            order.startPreparation();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EM_PREPARO);
        }

        @Test
        @DisplayName("sincroniza itens para EM_PREPARO")
        void startPreparation_syncsItems() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            order.startPreparation();
            order.getItems().forEach(i ->
                    assertThat(i.getStatus()).isEqualTo(OrderItemStatus.EM_PREPARO));
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("markReady()")
    class MarkReady {

        @Test
        @DisplayName("muda status de EM_PREPARO para PRONTO")
        void markReady_success() {
            Order order = orderWithStatus(OrderStatus.EM_PREPARO);
            order.markReady();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PRONTO);
        }

        @Test
        @DisplayName("sincroniza itens para PRONTO")
        void markReady_syncsItems() {
            Order order = orderWithStatus(OrderStatus.EM_PREPARO);
            order.markReady();
            order.getItems().forEach(i ->
                    assertThat(i.getStatus()).isEqualTo(OrderItemStatus.PRONTO));
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("sendForDelivery()")
    class SendForDelivery {

        @Test
        @DisplayName("lança IllegalStateException quando tipo não é DELIVERY")
        void sendForDelivery_throwsWhenNotDelivery() {
            Order order = orderWithStatus(OrderStatus.PRONTO);
            assertThatThrownBy(order::sendForDelivery)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("delivery");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("requestPayment()")
    class RequestPayment {

        @Test
        @DisplayName("muda status para AGUARDANDO_PAGAMENTO e comanda para EM_FECHAMENTO")
        void requestPayment_success() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            order.requestPayment();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.AGUARDANDO_PAGAMENTO);
            assertThat(order.getComandaStatus()).isEqualTo(ComandaStatus.EM_FECHAMENTO);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("muda status de PRONTO para ENTREGUE")
        void complete_success() {
            Order order = orderWithStatus(OrderStatus.PRONTO);
            order.complete();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.ENTREGUE);
        }

        @Test
        @DisplayName("isFinalized() retorna true após complete()")
        void complete_isFinalized() {
            Order order = orderWithStatus(OrderStatus.PRONTO);
            order.complete();
            assertThat(order.isFinalized()).isTrue();
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("closeComanda()")
    class CloseComanda {

        @Test
        @DisplayName("fecha comanda aberta com sucesso")
        void closeComanda_success() {
            Order order = pendingBalcao();
            order.closeComanda();
            assertThat(order.getComandaStatus()).isEqualTo(ComandaStatus.FECHADA);
        }

        @Test
        @DisplayName("lança IllegalStateException quando comanda já fechada")
        void closeComanda_throwsWhenAlreadyClosed() {
            Order order = pendingBalcao();
            order.closeComanda();
            assertThatThrownBy(order::closeComanda)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("já está fechada");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("cancela pedido PENDENTE, preenche campos e fecha comanda")
        void cancel_pending_success() {
            Order order = pendingBalcao();
            order.cancel("Desistência", "gerente");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELADO);
            assertThat(order.getCancelReason()).isEqualTo("Desistência");
            assertThat(order.getCanceledBy()).isEqualTo("gerente");
            assertThat(order.getCanceledAt()).isNotNull();
            assertThat(order.getComandaStatus()).isEqualTo(ComandaStatus.FECHADA);
        }

        @Test
        @DisplayName("cancela todos os itens ativos")
        void cancel_cancelsAllActiveItems() {
            Order order = pendingBalcao();
            order.cancel("Motivo", "ops");
            order.getItems().forEach(i ->
                    assertThat(i.getStatus().isCanceled()).isTrue());
        }

        @Test
        @DisplayName("lança IllegalStateException ao tentar cancelar pedido ENTREGUE")
        void cancel_throwsWhenDelivered() {
            Order order = orderWithStatus(OrderStatus.ENTREGUE);
            assertThatThrownBy(() -> order.cancel("X", "Y"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição inválida");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cancelItem()")
    class CancelItem {

        @Test
        @DisplayName("cancela item em pedido ENVIADO com status CANCELADO_ANTES_PREPARO")
        void cancelItem_enviado_beforePrep() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            UUID itemId = order.getItems().get(0).getId();
            order.cancelItem(itemId, "Cliente pediu");
            assertThat(order.getItems().get(0).getStatus())
                    .isEqualTo(OrderItemStatus.CANCELADO_ANTES_PREPARO);
        }

        @Test
        @DisplayName("cancela item em pedido EM_PREPARO com status CANCELADO_EM_PREPARO")
        void cancelItem_emPreparo_duringPrep() {
            Order order = orderWithStatus(OrderStatus.EM_PREPARO);
            UUID itemId = order.getItems().get(0).getId();
            order.cancelItem(itemId, "Intolerância");
            assertThat(order.getItems().get(0).getStatus())
                    .isEqualTo(OrderItemStatus.CANCELADO_EM_PREPARO);
        }

        @Test
        @DisplayName("lança IllegalStateException quando comanda está FECHADA")
        void cancelItem_throwsWhenComandaFechada() {
            Order order = pendingBalcao();
            order.closeComanda();
            UUID itemId = order.getItems().get(0).getId();
            assertThatThrownBy(() -> order.cancelItem(itemId, "X"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Comanda fechada");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando pedido CANCELADO")
        void cancelItem_throwsWhenOrderCanceled() {
            Order order = pendingBalcao();
            UUID itemId = order.getItems().get(0).getId();
            order.cancel("X", "Y");
            // Now status is CANCELADO but comanda is FECHADA — comanda check fires first
            assertThatThrownBy(() -> order.cancelItem(itemId, "Z"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando pedido PENDENTE")
        void cancelItem_throwsWhenPendente() {
            Order order = pendingBalcao();
            UUID itemId = order.getItems().get(0).getId();
            assertThatThrownBy(() -> order.cancelItem(itemId, "X"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pendentes");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando itemId não encontrado")
        void cancelItem_throwsWhenItemNotFound() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            assertThatThrownBy(() -> order.cancelItem(UUID.randomUUID(), "X"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("appendItems()")
    class AppendItems {

        @Test
        @DisplayName("adiciona itens a pedido ENVIADO e os marca como ENVIADO")
        void appendItems_success() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            List<OrderItem> newItems = List.of(item("Novo1"), item("Novo2"));
            order.appendItems(newItems);
            assertThat(order.getItemCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando lista vazia")
        void appendItems_throwsWhenEmpty() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            assertThatThrownBy(() -> order.appendItems(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ao menos um item");
        }

        @Test
        @DisplayName("lança NullPointerException quando lista nula")
        void appendItems_throwsWhenNull() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            assertThatThrownBy(() -> order.appendItems(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lança IllegalStateException quando pedido CANCELADO")
        void appendItems_throwsWhenCanceled() {
            Order order = pendingBalcao();
            order.cancel("X", "Y");
            assertThatThrownBy(() -> order.appendItems(List.of(item("X"))))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("replaceItems()")
    class ReplaceItems {

        @Test
        @DisplayName("substitui itens em pedido PENDENTE com sucesso")
        void replaceItems_success() {
            Order order = pendingBalcao();
            List<OrderItem> replacement = List.of(item("R1"), item("R2"));
            order.replaceItems(replacement);
            assertThat(order.getItemCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("lança IllegalStateException quando status não é PENDENTE")
        void replaceItems_throwsWhenNotPending() {
            Order order = orderWithStatus(OrderStatus.ENVIADO);
            assertThatThrownBy(() -> order.replaceItems(List.of(item("X"))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PENDENTE");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando lista vazia")
        void replaceItems_throwsWhenEmpty() {
            Order order = pendingBalcao();
            assertThatThrownBy(() -> order.replaceItems(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ao menos um item");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("registerPayment()")
    class RegisterPayment {

        @Test
        @DisplayName("registra método de pagamento em maiúsculas e sem espaços")
        void registerPayment_success() {
            Order order = pendingBalcao();
            order.registerPayment("  pix  ");
            assertThat(order.getPaymentMethod()).isEqualTo("PIX");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando método em branco")
        void registerPayment_throwsWhenBlank() {
            Order order = pendingBalcao();
            assertThatThrownBy(() -> order.registerPayment("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pagamento");
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando método nulo")
        void registerPayment_throwsWhenNull() {
            Order order = pendingBalcao();
            assertThatThrownBy(() -> order.registerPayment(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTotal()")
    class GetTotal {

        @Test
        @DisplayName("retorna soma dos itens quando há itens")
        void getTotal_sumOfItems() {
            Order order = pendingBalcao(); // 1 item at 10.00
            order.addItem(item("B")); // more 10.00
            assertThat(order.getTotal()).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("retorna zero quando pedido não tem itens")
        void getTotal_zeroWhenEmpty() {
            Order order = Order.builder()
                    .id(UUID.randomUUID())
                    .customerName("X")
                    .type(OrderType.BALCAO)
                    .status(OrderStatus.PENDENTE)
                    .createdAt(LocalDateTime.now())
                    .items(new ArrayList<>())
                    .build();
            assertThat(order.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("equals() e hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("dois pedidos com mesmo id são iguais")
        void equals_sameId() {
            UUID id = UUID.randomUUID();
            Order o1 = Order.builder().id(id).customerName("A")
                    .type(OrderType.BALCAO).status(OrderStatus.PENDENTE)
                    .createdAt(LocalDateTime.now()).build();
            Order o2 = Order.builder().id(id).customerName("B")
                    .type(OrderType.MESA).status(OrderStatus.ENVIADO)
                    .createdAt(LocalDateTime.now()).build();
            assertThat(o1).isEqualTo(o2);
            assertThat(o1.hashCode()).isEqualTo(o2.hashCode());
        }

        @Test
        @DisplayName("dois pedidos com ids diferentes não são iguais")
        void equals_differentId() {
            Order o1 = pendingBalcao();
            Order o2 = pendingBalcao();
            assertThat(o1).isNotEqualTo(o2);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("isDelivery()")
    class IsDelivery {

        @Test
        @DisplayName("retorna true para tipo DELIVERY")
        void isDelivery_trueForDelivery() {
            Order order = Order.builder()
                    .id(UUID.randomUUID()).customerName("X")
                    .type(OrderType.DELIVERY).status(OrderStatus.PENDENTE)
                    .createdAt(LocalDateTime.now()).build();
            assertThat(order.isDelivery()).isTrue();
        }

        @Test
        @DisplayName("retorna false para tipo BALCAO")
        void isDelivery_falseForBalcao() {
            assertThat(pendingBalcao().isDelivery()).isFalse();
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("builder() — campos obrigatórios")
    class BuilderValidation {

        @Test
        @DisplayName("lança NullPointerException quando id ausente")
        void builder_throwsWhenIdNull() {
            assertThatThrownBy(() -> Order.builder()
                    .customerName("X").type(OrderType.BALCAO)
                    .status(OrderStatus.PENDENTE).createdAt(LocalDateTime.now())
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ID");
        }

        @Test
        @DisplayName("lança NullPointerException quando customerName ausente")
        void builder_throwsWhenCustomerNameNull() {
            assertThatThrownBy(() -> Order.builder()
                    .id(UUID.randomUUID()).type(OrderType.BALCAO)
                    .status(OrderStatus.PENDENTE).createdAt(LocalDateTime.now())
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
