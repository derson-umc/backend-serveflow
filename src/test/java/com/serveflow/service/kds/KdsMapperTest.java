package com.serveflow.service.kds;

import com.serveflow.dto.kds.response.KdsOrderOutput;
import com.serveflow.dto.order.response.OrderOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KdsMapperTest {

    private final KdsMapper mapper = new KdsMapper();

    private OrderOutput buildOrderOutput(List<OrderOutput.OrderItemOutput> items) {
        return new OrderOutput(
                UUID.randomUUID(),
                "Cliente Teste",
                null,
                "BALCAO",
                "ENVIADO",
                "ABERTA",
                LocalDateTime.now(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("50.00"),
                items
        );
    }

    @Test
    @DisplayName("mapeia OrderOutput para KdsOrderOutput com campos corretos")
    void toOutput_mapsFieldsCorrectly() {
        OrderOutput.ItemAdditionalOutput adicional = new OrderOutput.ItemAdditionalOutput(
                UUID.randomUUID(), "Queijo Extra", 2, new BigDecimal("3.00"), new BigDecimal("6.00"));

        OrderOutput.OrderItemOutput item = new OrderOutput.OrderItemOutput(
                UUID.randomUUID(), UUID.randomUUID(), "X-Burguer", 1,
                new BigDecimal("25.00"), "Sem cebola", new BigDecimal("31.00"),
                List.of(adicional), "ENVIADO", null, "LANCHE");

        OrderOutput order = buildOrderOutput(List.of(item));

        KdsOrderOutput output = mapper.toOutput(order);

        assertThat(output.id()).isEqualTo(order.id());
        assertThat(output.customerName()).isEqualTo("Cliente Teste");
        assertThat(output.type()).isEqualTo("BALCAO");
        assertThat(output.status()).isEqualTo("ENVIADO");
        assertThat(output.items()).hasSize(1);
        assertThat(output.items().get(0).productName()).isEqualTo("X-Burguer");
        assertThat(output.items().get(0).quantity()).isEqualTo(1);
        assertThat(output.items().get(0).observation()).isEqualTo("Sem cebola");
        // adicional with quantity > 1 should have "x2" suffix
        assertThat(output.items().get(0).additionals()).contains("Queijo Extra x2");
    }

    @Test
    @DisplayName("adicional com quantidade 1 não tem sufixo")
    void toOutput_additionalsWithQuantityOne_noSuffix() {
        OrderOutput.ItemAdditionalOutput adicional = new OrderOutput.ItemAdditionalOutput(
                UUID.randomUUID(), "Bacon", 1, new BigDecimal("2.00"), new BigDecimal("2.00"));

        OrderOutput.OrderItemOutput item = new OrderOutput.OrderItemOutput(
                UUID.randomUUID(), UUID.randomUUID(), "X-Bacon", 1,
                new BigDecimal("25.00"), null, new BigDecimal("27.00"),
                List.of(adicional), "ENVIADO", null, "LANCHE");

        OrderOutput order = buildOrderOutput(List.of(item));
        KdsOrderOutput output = mapper.toOutput(order);

        assertThat(output.items().get(0).additionals()).contains("Bacon");
        assertThat(output.items().get(0).additionals().get(0)).doesNotContain("x1");
    }

    @Test
    @DisplayName("mapeia pedido sem itens")
    void toOutput_emptyItems() {
        OrderOutput order = buildOrderOutput(List.of());
        KdsOrderOutput output = mapper.toOutput(order);
        assertThat(output.items()).isEmpty();
    }
}
