package com.serveflow.service.order;

import com.serveflow.service.cashier.CashierEventPublisher;
import com.serveflow.service.kds.KdsEventPublisher;
import com.serveflow.service.kds.KdsMapper;
import com.serveflow.dto.order.request.ItemAdditionalInput;
import com.serveflow.dto.order.request.OrderItemInput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.integration.AddressResolver;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceExtendedTest {

    @Mock OrderRepository orderRepository;
    @Mock AddressResolver addressResolver;
    @Mock StockService stockService;
    @Mock MenuRepository menuRepository;
    @Mock KdsEventPublisher kdsEventPublisher;
    @Mock CashierEventPublisher cashierEventPublisher;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock KdsMapper kdsMapper;

    @InjectMocks OrderService service;

    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
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

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna OrderOutput quando pedido encontrado")
        void findById_returnsOutput() {
            Order order = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);

            OrderOutput result = service.findById(orderId);

            assertThat(result.id()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("propaga OrderNotFoundException quando não encontrado")
        void findById_throwsWhenNotFound() {
            when(orderRepository.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));

            assertThatThrownBy(() -> service.findById(orderId))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("retorna lista de todos os pedidos")
        void findAll_returnsList() {
            Order o1 = buildOrder(UUID.randomUUID(), OrderStatus.PENDENTE, OrderType.BALCAO);
            Order o2 = buildOrder(UUID.randomUUID(), OrderStatus.ENVIADO, OrderType.MESA);
            when(orderRepository.findAll()).thenReturn(List.of(o1, o2));

            List<OrderOutput> result = service.findAll();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("retorna lista vazia quando não há pedidos")
        void findAll_returnsEmpty() {
            when(orderRepository.findAll()).thenReturn(List.of());

            List<OrderOutput> result = service.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("appendItems")
    class AppendItems {

        @Test
        @DisplayName("adiciona itens a pedido ENVIADO com sucesso")
        void appendItems_success_whenEnviado() {
            Order order = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(saved);

            List<OrderItemInput> newItems = List.of(
                    new OrderItemInput(UUID.randomUUID(), "Novo Item", 1,
                            new BigDecimal("10.00"), null, "LANCHE", List.of()));

            OrderOutput result = service.appendItems(orderId, newItems);

            assertThat(result).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando lista de itens vazia")
        void appendItems_throwsWhenEmpty() {
            assertThatThrownBy(() -> service.appendItems(orderId, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ao menos um item");

            verify(orderRepository, never()).findById(any());
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando lista nula")
        void appendItems_throwsWhenNull() {
            assertThatThrownBy(() -> service.appendItems(orderId, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando EM_PREPARO e item não é bebida")
        void appendItems_throwsWhenEmPreparo_nonBeverageItem() {
            Order order = buildOrder(orderId, OrderStatus.EM_PREPARO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);

            List<OrderItemInput> nonBeverageItems = List.of(
                    new OrderItemInput(UUID.randomUUID(), "Hamburguer", 1,
                            new BigDecimal("20.00"), null, "LANCHE", List.of()));

            assertThatThrownBy(() -> service.appendItems(orderId, nonBeverageItems))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("bebidas");
        }

        @Test
        @DisplayName("permite adicionar bebida quando pedido está EM_PREPARO")
        void appendItems_allowsBeverage_whenEmPreparo() {
            Order order = buildOrder(orderId, OrderStatus.EM_PREPARO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.EM_PREPARO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(saved);

            List<OrderItemInput> beverageItems = List.of(
                    new OrderItemInput(UUID.randomUUID(), "Suco", 1,
                            new BigDecimal("8.00"), null, "BEBIDA_NAO_ALCOOLICA", List.of()));

            OrderOutput result = service.appendItems(orderId, beverageItems);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateItems")
    class UpdateItems {

        @Test
        @DisplayName("substitui itens em pedido PENDENTE com sucesso")
        void updateItems_success() {
            Order order = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            doNothing().when(stockService).validateRecipesForOrder(any());
            Order saved = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(saved);

            List<OrderItemInput> newItems = List.of(
                    new OrderItemInput(UUID.randomUUID(), "Novo Prato", 2,
                            new BigDecimal("30.00"), null, "PRATO", List.of()));

            OrderOutput result = service.updateItems(orderId, newItems);

            assertThat(result).isNotNull();
            verify(stockService).validateRecipesForOrder(any());
        }

        @Test
        @DisplayName("lança IllegalArgumentException quando lista vazia")
        void updateItems_throwsWhenEmpty() {
            assertThatThrownBy(() -> service.updateItems(orderId, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ao menos um item");
        }
    }

    @Nested
    @DisplayName("startPreparation")
    class StartPreparation {

        @Test
        @DisplayName("inicia preparo a partir de ENVIADO sem confirmar novamente")
        void startPreparation_fromEnviado() {
            Order order = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order savedAfterPrep = buildOrder(orderId, OrderStatus.EM_PREPARO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(savedAfterPrep);

            OrderOutput result = service.startPreparation(orderId);

            assertThat(result.status()).isEqualTo("EM_PREPARO");
            verify(stockService, never()).validateRecipesForOrder(any());
        }

        @Test
        @DisplayName("confirma e inicia preparo a partir de PENDENTE")
        void startPreparation_fromPendente_confirmsFirst() {
            Order pendingOrder = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(pendingOrder);
            doNothing().when(stockService).validateRecipesForOrder(any());
            doNothing().when(stockService).validateStockForOrder(any());
            Order confirmedOrder = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            Order prepOrder = buildOrder(orderId, OrderStatus.EM_PREPARO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class)))
                    .thenReturn(confirmedOrder)
                    .thenReturn(prepOrder);
            doNothing().when(stockService).deductForOrder(any(), any());

            OrderOutput result = service.startPreparation(orderId);

            assertThat(result.status()).isEqualTo("EM_PREPARO");
            verify(stockService).validateRecipesForOrder(any());
            verify(stockService).validateStockForOrder(any());
            verify(stockService).deductForOrder(any(), any());
        }
    }

    @Nested
    @DisplayName("requestPayment")
    class RequestPayment {

        @Test
        @DisplayName("solicita pagamento a partir de ENVIADO")
        void requestPayment_fromEnviado() {
            Order order = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.AGUARDANDO_PAGAMENTO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(saved);
            doNothing().when(cashierEventPublisher).publishBillCloseRequested(any(), any(), any());

            OrderOutput result = service.requestPayment(orderId);

            assertThat(result.status()).isEqualTo("AGUARDANDO_PAGAMENTO");
        }

        @Test
        @DisplayName("confirma e solicita pagamento a partir de PENDENTE")
        void requestPayment_fromPendente_confirmsFirst() {
            Order pendingOrder = buildOrder(orderId, OrderStatus.PENDENTE, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(pendingOrder);
            doNothing().when(stockService).validateRecipesForOrder(any());
            doNothing().when(stockService).validateStockForOrder(any());
            Order confirmedOrder = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            Order paymentOrder = buildOrder(orderId, OrderStatus.AGUARDANDO_PAGAMENTO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class)))
                    .thenReturn(confirmedOrder)
                    .thenReturn(paymentOrder);
            doNothing().when(stockService).deductForOrder(any(), any());
            doNothing().when(cashierEventPublisher).publishBillCloseRequested(any(), any(), any());

            OrderOutput result = service.requestPayment(orderId);

            assertThat(result.status()).isEqualTo("AGUARDANDO_PAGAMENTO");
            verify(stockService).deductForOrder(any(), any());
        }
    }

    @Nested
    @DisplayName("markReady")
    class MarkReady {

        @Test
        @DisplayName("marca pedido como PRONTO a partir de EM_PREPARO")
        void markReady_fromEmPreparo() {
            Order order = buildOrder(orderId, OrderStatus.EM_PREPARO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.PRONTO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(saved);

            OrderOutput result = service.markReady(orderId);

            assertThat(result.status()).isEqualTo("PRONTO");
        }
    }

    @Nested
    @DisplayName("cancelItem")
    class CancelItem {

        @Test
        @DisplayName("cancela item em pedido ENVIADO")
        void cancelItem_success() {
            Order order = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            UUID itemId = order.getItems().get(0).getId();
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.ENVIADO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(saved);

            OrderOutput result = service.cancelItem(orderId, itemId, "Cliente mudou de ideia");

            assertThat(result).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("sendForDelivery")
    class SendForDelivery {

        @Test
        @DisplayName("envia pedido DELIVERY para entrega")
        void sendForDelivery_success() {
            Order order = buildOrder(orderId, OrderStatus.PRONTO, OrderType.DELIVERY);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.A_CAMINHO, OrderType.DELIVERY);
            when(orderRepository.save(any(Order.class))).thenReturn(saved);

            OrderOutput result = service.sendForDelivery(orderId);

            assertThat(result.status()).isEqualTo("A_CAMINHO");
        }
    }

    @Nested
    @DisplayName("cancel — recordLossForOrder")
    class CancelRecordLoss {

        @Test
        @DisplayName("registra perda de estoque quando cancela pedido EM_PREPARO")
        void cancel_recordsLoss_whenEmPreparo() {
            Order order = buildOrder(orderId, OrderStatus.EM_PREPARO, OrderType.BALCAO);
            when(orderRepository.findById(orderId)).thenReturn(order);
            Order saved = buildOrder(orderId, OrderStatus.CANCELADO, OrderType.BALCAO);
            when(orderRepository.save(any(Order.class))).thenReturn(saved);
            when(menuRepository.findByActiveOrderId(any())).thenReturn(Optional.empty());
            doNothing().when(stockService).recordLossForOrder(any(), any(), any());

            service.cancel(orderId, "Desistiu", "operador");

            verify(stockService).recordLossForOrder(any(), any(), any());
        }
    }
}
