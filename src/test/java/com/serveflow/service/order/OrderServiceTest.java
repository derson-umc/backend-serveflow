package com.serveflow.service.order;

import com.serveflow.service.kds.KdsEventPublisher;
import com.serveflow.dto.order.request.OrderInput;
import com.serveflow.events.OrderCompletedEvent;
import com.serveflow.dto.order.request.OrderItemInput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.integration.AddressResolver;
import com.serveflow.model.address.Address;
import com.serveflow.model.order.Order;
import com.serveflow.model.order.OrderItem;
import com.serveflow.model.order.OrderItemStatus;
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
        @DisplayName("cria pedido BALCAO sem pagamento e publica evento KDS.")
        void create_criaPedidoBalcao_semPagamento() {
            OrderInput input = balcaoOrderInput(null);
            when(addressResolver.resolve(any())).thenReturn(null);
            doNothing().when(stockService).validateRecipesForOrder(any());
            Order created = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(created);

            OrderOutput result = service.create(input);

            assertThat(result.id()).isEqualTo(orderId);
            assertThat(result.status()).isEqualTo("PENDENTE");
            verify(orderRepository).save(any(Order.class));
            verify(stockService).validateRecipesForOrder(any());
        }

        @Test
        @DisplayName("cria pedido com método de pagamento e registra corretamente.")
        void create_registraPagamento_whenMetodoFornecido() {
            OrderInput input = balcaoOrderInput("PIX");
            when(addressResolver.resolve(any())).thenReturn(null);
            doNothing().when(stockService).validateRecipesForOrder(any());
            Order created = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            created.registerPayment("PIX");
            when(orderRepository.save(any(Order.class))).thenReturn(created);

            OrderOutput result = service.create(input);

            assertThat(result.paymentMethod()).isEqualTo("PIX");
        }

        @Test
        @DisplayName("KDS event failure não interrompe a criação do pedido.")
        void create_kdsFailure_naoInterrompeFluxo() {
            OrderInput input = balcaoOrderInput(null);
            when(addressResolver.resolve(any())).thenReturn(null);
            doNothing().when(stockService).validateRecipesForOrder(any());
            Order created = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(created);

            // publishKdsSafely silencia exceções; a criação deve concluir normalmente mesmo sem KdsMapper
            OrderOutput result = service.create(input);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("retorna pedidos com status PENDENTE.")
        void findByStatus_retornaPedidos_paraPENDENTE() {
            Order order = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.findByStatus(OrderStatus.PENDENTE)).thenReturn(List.of(order));

            List<OrderOutput> result = service.findByStatus("PENDENTE");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo("PENDENTE");
        }

        @Test
        @DisplayName("retorna lista vazia quando não existem pedidos com o status.")
        void findByStatus_retornaVazio_whenNenhumPedidoComStatus() {
            when(orderRepository.findByStatus(OrderStatus.EM_PREPARO)).thenReturn(List.of());

            List<OrderOutput> result = service.findByStatus("EM_PREPARO");

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
        @DisplayName("confirma pedido PENDENTE, valida estoque e deduz insumos.")
        void confirm_confirma_eDeduEstoque() {
            Order order = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            doNothing().when(stockService).validateStockForOrder(any());
            Order saved = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            when(orderRepository.save(order)).thenReturn(saved);
            doNothing().when(stockService).deductForOrder(any(), any());

            OrderOutput result = service.confirm(orderId);

            assertThat(result.status()).isEqualTo("ENVIADO");
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
        @DisplayName("cancela pedido PENDENTE sem restaurar estoque.")
        void cancel_semRestaurarEstoque_whenStatusPENDENTE() {
            Order order = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELADO, OrderType.BALCAO);
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());

            service.cancel(orderId, null, "operador");

            verify(stockService, never()).restoreForOrder(any(), any());
        }

        @Test
        @DisplayName("cancela pedido ENVIADO e restaura estoque.")
        void cancel_restauraEstoque_whenStatusENVIADO() {
            Order order = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELADO, OrderType.BALCAO);
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());
            doNothing().when(stockService).restoreForOrder(any(), any());

            service.cancel(orderId, "Pedido duplicado", "operador");

            verify(stockService).restoreForOrder(any(), any());
        }

        @Test
        @DisplayName("cancela pedido EM_PREPARO e restaura estoque.")
        void cancel_restauraEstoque_whenStatusEM_PREPARO() {
            Order order = buildOrder(orderId, OrderStatus.EM_PREPARO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELADO, OrderType.BALCAO);
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());
            doNothing().when(stockService).restoreForOrder(any(), any());

            service.cancel(orderId, "Cliente desistiu", "gerente");

            verify(stockService).restoreForOrder(any(), any());
        }

        @Test
        @DisplayName("falha no restore de estoque propaga exceção.")
        void cancel_propagaExcecao_whenRestoreFalha() {
            Order order = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELADO, OrderType.BALCAO);
            when(orderRepository.save(order)).thenReturn(saved);
            doThrow(new RuntimeException("Estoque inconsistente")).when(stockService)
                    .restoreForOrder(any(), any());

            assertThatThrownBy(() -> service.cancel(orderId, null, "operador"))
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
            Order order = buildOrder(orderId, OrderStatus.PRONTO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.ENTREGUE, OrderType.BALCAO);
            when(orderRepository.save(order)).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());

            OrderOutput result = service.complete(orderId);

            assertThat(result.status()).isEqualTo("ENTREGUE");
            verify(eventPublisher).publishEvent(any(OrderCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("settleFromCashier")
    class SettleFromCashier {

        @Test
        @DisplayName("liquida pedido via caixa com método de pagamento e completa.")
        void settle_completaPedido_comPagamento() {
            Order order = buildOrder(orderId, OrderStatus.PRONTO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.ENTREGUE, OrderType.BALCAO);
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
        items.add(new OrderItem(UUID.randomUUID(), UUID.randomUUID(), "Produto Teste", 2,
                new BigDecimal("15.00"), null, List.of(), OrderItemStatus.ENVIADO, null, null));
        return Order.builder()
                .id(id)
                .customerName("Cliente Teste")
                .type(type)
                .status(status)
                .createdAt(LocalDateTime.now())
                .items(items)
                .build();
    }

    private OrderInput balcaoOrderInput(String paymentMethod) {
        List<OrderItemInput> items = List.of(
                new OrderItemInput(UUID.randomUUID(), "Produto", 1,
                        new BigDecimal("20.00"), null, null, List.of()));
        return new OrderInput("Cliente", null, "BALCAO", null, paymentMethod, null, items);
    }
}
