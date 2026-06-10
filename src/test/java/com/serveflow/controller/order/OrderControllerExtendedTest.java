package com.serveflow.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.order.request.OrderInput;
import com.serveflow.dto.order.request.OrderItemInput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.order.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerExtendedTest {

    @Mock
    OrderService orderService;
    @Mock
    AuditService auditService;

    @InjectMocks
    OrderController controller;

    MockMvc mvc;
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        User mockUser = new User(1L, "admin", "admin@test.com", "pass", UserRole.ADMIN, "Administrador");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities()));
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler(auditService))
                .build();
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private OrderOutput orderOutput(UUID id, String status) {
        return new OrderOutput(
                id, "Cliente Teste", null, "BALCAO", status, "ABERTA",
                LocalDateTime.of(2026, 1, 1, 12, 0), null, null, null, null, null, null,
                null, new BigDecimal("29.90"), List.of());
    }

    private String json(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    @Nested
    @DisplayName("POST /orders")
    class Create {

        @Test
        @DisplayName("retorna 201 com pedido criado")
        void create_returns201() throws Exception {
            UUID id = UUID.randomUUID();
            OrderOutput out = orderOutput(id, "PENDENTE");
            when(orderService.create(any(OrderInput.class), any())).thenReturn(out);

            OrderItemInput item = new OrderItemInput(UUID.randomUUID(), "Produto", 1,
                    new BigDecimal("15.00"), null, "LANCHE", List.of());
            OrderInput input = new OrderInput("Cliente", null, "BALCAO", null, null, null, List.of(item));

            mvc.perform(post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()));

            verify(orderService).create(any(OrderInput.class), any());
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/request-payment")
    class RequestPayment {

        @Test
        @DisplayName("retorna 200 com pedido aguardando pagamento")
        void requestPayment_returns200() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.requestPayment(id)).thenReturn(orderOutput(id, "AGUARDANDO_PAGAMENTO"));

            mvc.perform(patch("/orders/{id}/request-payment", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"));

            verify(orderService).requestPayment(id);
        }

        @Test
        @DisplayName("retorna 404 quando pedido não existe")
        void requestPayment_returns404() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.requestPayment(id)).thenThrow(new OrderNotFoundException(id));

            mvc.perform(patch("/orders/{id}/request-payment", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/items/{itemId}/cancel")
    class CancelItem {

        @Test
        @DisplayName("retorna 200 com item cancelado")
        void cancelItem_returns200() throws Exception {
            UUID orderId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();
            when(orderService.cancelItem(eq(orderId), eq(itemId), any()))
                    .thenReturn(orderOutput(orderId, "ENVIADO"));

            mvc.perform(patch("/orders/{id}/items/{itemId}/cancel", orderId, itemId))
                    .andExpect(status().isOk());

            verify(orderService).cancelItem(eq(orderId), eq(itemId), any());
        }
    }

    @Nested
    @DisplayName("POST /orders/{id}/items/add")
    class AddItems {

        @Test
        @DisplayName("retorna 200 com itens adicionados")
        void addItems_returns200() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.appendItems(eq(id), any())).thenReturn(orderOutput(id, "ENVIADO"));

            List<OrderItemInput> items = List.of(
                    new OrderItemInput(UUID.randomUUID(), "Suco", 1, new BigDecimal("8.00"), null, "BEBIDA_NAO_ALCOOLICA", List.of()));

            mvc.perform(post("/orders/{id}/items/add", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(items)))
                    .andExpect(status().isOk());

            verify(orderService).appendItems(eq(id), any());
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/items")
    class UpdateItems {

        @Test
        @DisplayName("retorna 200 com itens atualizados")
        void updateItems_returns200() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.updateItems(eq(id), any())).thenReturn(orderOutput(id, "PENDENTE"));

            List<OrderItemInput> items = List.of(
                    new OrderItemInput(UUID.randomUUID(), "Novo Prato", 2, new BigDecimal("30.00"), null, "PRATO", List.of()));

            mvc.perform(patch("/orders/{id}/items", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(items)))
                    .andExpect(status().isOk());

            verify(orderService).updateItems(eq(id), any());
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/send")
    class SendForDelivery {

        @Test
        @DisplayName("retorna 200 com pedido enviado para entrega")
        void send_returns200() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.sendForDelivery(id)).thenReturn(orderOutput(id, "A_CAMINHO"));

            mvc.perform(patch("/orders/{id}/send", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("A_CAMINHO"));

            verify(orderService).sendForDelivery(id);
        }
    }
}
