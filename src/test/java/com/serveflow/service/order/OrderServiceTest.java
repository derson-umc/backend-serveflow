package com.serveflow.service.order;

import com.serveflow.controller.kds.KdsEventPublisher;
import com.serveflow.dto.order.request.OrderInput;
import com.serveflow.events.OrderCompletedEvent;
import com.serveflow.dto.order.request.OrderItemInput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.integration.AddressResolver;
import com.serveflow.model.address.Address;
import com.serveflow.model.order.Order;
import com.serveflow.model.order.OrderItem;
import com.serveflow.model.order.OrderStatus;
import com.serveflow.model.order.OrderType;
import com.serveflow.repository.menu.MenuRepository;
import com.serveflow.repository.order.OrderRepository;
import com.serveflow.service.stock.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock AddressResolver addressResolver;
    @Mock StockService stockService;
    @Mock MenuRepository menuRepository;
    @Mock KdsEventPublisher kdsEventPublisher;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks OrderService service;

    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("cria pedido LOCAL sem pagamento e publica evento KDS.")
        void create_criaPedidoLocal_semPagamento() {
            OrderInput input = localOrderInput(null);
            when(addressResolver.resolve(any())).thenReturn(null);
            doNothing().when(stockService).validateRecipesForOrder(any());
            Order created = buildOrder(orderId, OrderStatus.CREATED, OrderType.LOCAL);
            when(orderRepository.save(any(Order.class))).thenReturn(created);

            OrderOutput result = service.create(input);

            assertThat(result.id()).isEqualTo(orderId);
            assertThat(result.status()).isEqualTo("CREATED");
            verify(orderRepository).save(any(Order.class));
            verify(stockService).validateRecipesForOrder(any());
        }

        @Test
        @DisplayName("cria pedido com método de pagamento e registra corretamente.")
        void create_registraPagamento_whenMetodoFornecido() {
            OrderInput input = localOrderInput("PIX");
            when(addressResolver.resolve(any())).thenReturn(null);
            doNothing().when(stockService).validateRecipesForOrder(any());
            Order created = buildOrder(orderId, OrderStatus.CREATED, OrderType.LOCAL);
            created.registerPayment("PIX");
            when(orderRepository.save(any(Order.class))).thenReturn(created);

            OrderOutput result = service.create(input);

            assertThat(result.paymentMethod()).isEqualTo("PIX");
        }

        @Test
        @DisplayName("KDS event failure não interrompe a criação do pedido.")
        void create_kdsFailure_naoInterrompeFluxo() {
            OrderInput input = localOrderInput(null);
            when(addressResolver.resolve(any())).thenReturn(null);
            doNothing().when(stockService).validateRecipesForOrder(any());
            Order created = buildOrder(orderId, OrderStatus.CREATED, OrderType.LOCAL);
            when(orderRepository.save(any(Order.class))).thenReturn(created);
            doThrow(new RuntimeException("KDS indisponível")).when(kdsEventPublisher).publishUpdate(any());

            OrderOutput result = service.create(input);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("retorna pedidos com status CREATED.")
        void findByStatus_retornaPedidos_paraCREATED() {
            Order order = buildOrder(orderId, OrderStatus.CREATED, OrderType.LOCAL);
            when(orderRepository.findByStatus(OrderStatus.CREATED)).thenReturn(List.of(order));

            List<OrderOutput> result = service.findByStatus("CREATED");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo("CREATED");
        }

        @Test
        @DisplayName("retorna lista vazia quando não existem pedidos com o status.")
        void findByStatus_retornaVazio_whenNenhumPedidoComStatus() {
            when(orderRepository.findByStatus(OrderStatus.IN_PREPARATION)).thenReturn(List.of());

            List<OrderOutput> result = service.findByStatus("IN_PREPARATION");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("lança IllegalArgumentException para status inválido.")
        void findByStatus_lancaExcecao_paraStatusInvalido() {
            assertThatThrownBy(() -> service.findByStatus("STATUS_INVALIDO"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("confirm")
    class Confirm {

        @Test
        @DisplayName("confirma pedido CREATED, valida estoque e deduz insumos.")
        void confirm_confirma_eDeduEstoque() {
            Order order = buildOrder(orderId, OrderStatus.CREATED, OrderType.LOCAL);
            when(orderRepository.findById(orderId)).thenReturn(order);
            doNothing().when(stockService).validateStockForOrder(any());
            Order saved = buildOrder(orderId, OrderStatus.CONFIRMED, OrderType.LOCAL);
            when(orderRepository.save(order)).thenReturn(saved);
            doNothing().when(stockService).deductForOrder(any(), any());

            OrderOutput result = service.confirm(orderId);

            assertThat(result.status()).isEqualTo("CONFIRMED");
            verify(stockService).validateStockForOrder(any());
            verify(stockService).deductForOrder(any(), any());
        }

        @Test
        @DisplayName("propaga OrderNotFoundException quando pedido não encontrado.")
        void confirm_propagaExcecao_whenPedidoNaoEncontrado() {
            when(orderRepository.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));

            assertThatThrownBy(() -> service.confirm(orderId))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(stockService, never()).validateStockForOrder(any());
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("cancela pedido CREATED sem restaurar estoque.")
        void cancel_semRestaurarEstoque_whenStatusCREATED() {
            Order order = buildOrder(orderId, OrderStatus.CREATED, OrderType.LOCAL);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELLED, OrderType.LOCAL);
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());

            service.cancel(orderId);

            verify(stockService, never()).restoreForOrder(any(), any());
        }

        @Test
        @DisplayName("cancela pedido CONFIRMED e restaura estoque.")
        void cancel_restauraEstoque_whenStatusCONFIRMED() {
            Order order = buildOrder(orderId, OrderStatus.CONFIRMED, OrderType.LOCAL);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELLED, OrderType.LOCAL);
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());
            doNothing().when(stockService).restoreForOrder(any(), any());

            service.cancel(orderId);

            verify(stockService).restoreForOrder(any(), any());
        }

        @Test
        @DisplayName("cancela pedido IN_PREPARATION e restaura estoque.")
        void cancel_restauraEstoque_whenStatusIN_PREPARATION() {
            Order order = buildOrder(orderId, OrderStatus.IN_PREPARATION, OrderType.LOCAL);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELLED, OrderType.LOCAL);
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());
            doNothing().when(stockService).restoreForOrder(any(), any());

            service.cancel(orderId);

            verify(stockService).restoreForOrder(any(), any());
        }

        @Test
        @DisplayName("falha no restore de estoque propaga exceção.")
        void cancel_propagaExcecao_whenRestoreFalha() {
            Order order = buildOrder(orderId, OrderStatus.CONFIRMED, OrderType.LOCAL);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELLED, OrderType.LOCAL);
            when(orderRepository.save(order)).thenReturn(saved);
            doThrow(new RuntimeException("Estoque inconsistente")).when(stockService)
                    .restoreForOrder(any(), any());

            assertThatThrownBy(() -> service.cancel(orderId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Estoque inconsistente");
        }
    }

    @Nested
    @DisplayName("complete")
    class Complete {

        @Test
        @DisplayName("completa pedido, libera menu e publica OrderCompletedEvent.")
        void complete_completaEPublicaEvento() {
            Order order = buildOrder(orderId, OrderStatus.READY, OrderType.LOCAL);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.DELIVERED, OrderType.LOCAL);
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());

            OrderOutput result = service.complete(orderId);

            assertThat(result.status()).isEqualTo("DELIVERED");
            verify(eventPublisher).publishEvent(any(OrderCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("settleFromCashier")
    class SettleFromCashier {

        @Test
        @DisplayName("liquida pedido via caixa com método de pagamento e completa.")
        void settle_completaPedido_comPagamento() {
            Order order = buildOrder(orderId, OrderStatus.READY, OrderType.LOCAL);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.DELIVERED, OrderType.LOCAL);
            saved.registerPayment("DINHEIRO");
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());

            OrderOutput result = service.settleFromCashier(orderId, "DINHEIRO");

            assertThat(result.paymentMethod()).isEqualTo("DINHEIRO");
            verify(eventPublisher).publishEvent(any(OrderCompletedEvent.class));
        }
    }

    private Order buildOrder(UUID id, OrderStatus status, OrderType type) {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(UUID.randomUUID(), "Produto Teste", 2,
                new BigDecimal("15.00"), null, List.of()));
        return new Order(id, "Cliente Teste", null, type, status,
                LocalDateTime.now(), null, null, items, null);
    }

    private OrderInput localOrderInput(String paymentMethod) {
        List<OrderItemInput> items = List.of(
                new OrderItemInput(UUID.randomUUID(), "Produto", 1,
                        new BigDecimal("20.00"), null, List.of()));
        return new OrderInput("Cliente", null, "LOCAL", null, paymentMethod, items);
    }
}
