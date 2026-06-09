package com.serveflow.controller.kds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.kds.response.KdsItemOutput;
import com.serveflow.dto.kds.response.KdsOrderOutput;
import com.serveflow.dto.order.response.OrderOutput;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.kds.KdsEventPublisher;
import com.serveflow.service.kds.KdsMapper;
import com.serveflow.service.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KdsControllerTest {

    @Mock
    OrderService orderService;
    @Mock
    KdsEventPublisher publisher;
    @Mock
    KdsMapper mapper;
    @Mock
    AuditService auditService;

    @InjectMocks
    KdsController controller;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(auditService))
                .build();
    }

    private OrderOutput orderOutput(UUID id, String status) {
        return new OrderOutput(id, "Cliente", null, "BALCAO", status, "ABERTA",
                LocalDateTime.now(), null, null, null, null, null, null,
                new BigDecimal("20.00"), List.of());
    }

    private KdsOrderOutput kdsOutput(UUID id, String status) {
        return new KdsOrderOutput(id, "Cliente", "BALCAO", status, "ABERTA",
                LocalDateTime.now(), List.of());
    }

    @Nested
    @DisplayName("GET /kds/orders")
    class OpenOrders {

        @Test
        @DisplayName("retorna 200 com pedidos ativos de todos os status")
        void openOrders_returns200() throws Exception {
            UUID id = UUID.randomUUID();
            OrderOutput out = orderOutput(id, "PENDENTE");
            KdsOrderOutput kdsOut = kdsOutput(id, "PENDENTE");

            when(orderService.findByStatus(any())).thenReturn(List.of(out));
            when(mapper.toOutput(any(OrderOutput.class))).thenReturn(kdsOut);

            mvc.perform(get("/kds/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            // 4 active statuses → findByStatus called 4 times
            verify(orderService, times(4)).findByStatus(any());
        }

        @Test
        @DisplayName("retorna 200 com lista vazia quando não há pedidos ativos")
        void openOrders_returnsEmpty() throws Exception {
            when(orderService.findByStatus(any())).thenReturn(List.of());

            mvc.perform(get("/kds/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("PATCH /kds/orders/{id}/confirm")
    class Confirm {

        @Test
        @DisplayName("retorna 200 com pedido confirmado")
        void confirm_returns200() throws Exception {
            UUID id = UUID.randomUUID();
            OrderOutput out = orderOutput(id, "ENVIADO");
            KdsOrderOutput kdsOut = kdsOutput(id, "ENVIADO");

            when(orderService.confirm(id)).thenReturn(out);
            when(mapper.toOutput(out)).thenReturn(kdsOut);

            mvc.perform(patch("/kds/orders/{id}/confirm", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ENVIADO"));
        }

        @Test
        @DisplayName("retorna 404 quando pedido não encontrado")
        void confirm_returns404() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.confirm(id)).thenThrow(new OrderNotFoundException(id));

            mvc.perform(patch("/kds/orders/{id}/confirm", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /kds/orders/{id}/prepare")
    class Prepare {

        @Test
        @DisplayName("retorna 200 com pedido em preparo e publica evento KDS")
        void prepare_returns200AndPublishes() throws Exception {
            UUID id = UUID.randomUUID();
            OrderOutput out = orderOutput(id, "EM_PREPARO");
            KdsOrderOutput kdsOut = kdsOutput(id, "EM_PREPARO");

            when(orderService.startPreparation(id)).thenReturn(out);
            when(mapper.toOutput(out)).thenReturn(kdsOut);

            mvc.perform(patch("/kds/orders/{id}/prepare", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("EM_PREPARO"));

            verify(publisher).publishUpdate(kdsOut);
        }

        @Test
        @DisplayName("retorna 404 quando pedido não encontrado")
        void prepare_returns404() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.startPreparation(id)).thenThrow(new OrderNotFoundException(id));

            mvc.perform(patch("/kds/orders/{id}/prepare", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /kds/orders/{id}/ready")
    class Ready {

        @Test
        @DisplayName("retorna 200 com pedido pronto e publica remove")
        void ready_returns200AndPublishesRemove() throws Exception {
            UUID id = UUID.randomUUID();
            OrderOutput out = orderOutput(id, "PRONTO");
            KdsOrderOutput kdsOut = kdsOutput(id, "PRONTO");

            when(orderService.markReady(id)).thenReturn(out);
            when(mapper.toOutput(out)).thenReturn(kdsOut);

            mvc.perform(patch("/kds/orders/{id}/ready", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PRONTO"));

            verify(publisher).publishRemove(id, "PRONTO");
        }
    }

    @Nested
    @DisplayName("PATCH /kds/orders/{id}/complete")
    class Complete {

        @Test
        @DisplayName("retorna 200 com pedido entregue e publica remove")
        void complete_returns200AndPublishesRemove() throws Exception {
            UUID id = UUID.randomUUID();
            OrderOutput out = orderOutput(id, "ENTREGUE");
            KdsOrderOutput kdsOut = kdsOutput(id, "ENTREGUE");

            when(orderService.complete(id)).thenReturn(out);
            when(mapper.toOutput(out)).thenReturn(kdsOut);

            mvc.perform(patch("/kds/orders/{id}/complete", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ENTREGUE"));

            verify(publisher).publishRemove(id, "ENTREGUE");
        }

        @Test
        @DisplayName("retorna 404 quando pedido não encontrado")
        void complete_returns404() throws Exception {
            UUID id = UUID.randomUUID();
            when(orderService.complete(id)).thenThrow(new OrderNotFoundException(id));

            mvc.perform(patch("/kds/orders/{id}/complete", id))
                    .andExpect(status().isNotFound());
        }
    }
}
