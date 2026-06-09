package com.serveflow.model.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Nested
    @DisplayName("isFinal()")
    class IsFinal {

        @Test
        @DisplayName("ENTREGUE é status final")
        void entregue_isFinal() {
            assertThat(OrderStatus.ENTREGUE.isFinal()).isTrue();
        }

        @Test
        @DisplayName("CANCELADO é status final")
        void cancelado_isFinal() {
            assertThat(OrderStatus.CANCELADO.isFinal()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"ENTREGUE", "CANCELADO"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("demais status não são finais")
        void nonFinal_statuses(OrderStatus status) {
            assertThat(status.isFinal()).isFalse();
        }
    }

    @Nested
    @DisplayName("isPendingPayment()")
    class IsPendingPayment {

        @Test
        @DisplayName("AGUARDANDO_PAGAMENTO retorna true")
        void aguardandoPagamento_isPendingPayment() {
            assertThat(OrderStatus.AGUARDANDO_PAGAMENTO.isPendingPayment()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"AGUARDANDO_PAGAMENTO"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("demais status não são pendentes de pagamento")
        void other_statuses_areNotPendingPayment(OrderStatus status) {
            assertThat(status.isPendingPayment()).isFalse();
        }
    }

    @Nested
    @DisplayName("canTransitionTo()")
    class CanTransitionTo {

        @Test
        @DisplayName("PENDENTE pode ir para ENVIADO")
        void pendente_canGoTo_enviado() {
            assertThat(OrderStatus.PENDENTE.canTransitionTo(OrderStatus.ENVIADO)).isTrue();
        }

        @Test
        @DisplayName("PENDENTE pode ir para CANCELADO")
        void pendente_canGoTo_cancelado() {
            assertThat(OrderStatus.PENDENTE.canTransitionTo(OrderStatus.CANCELADO)).isTrue();
        }

        @Test
        @DisplayName("PENDENTE não pode ir para EM_PREPARO diretamente")
        void pendente_cannotGoTo_emPreparo() {
            assertThat(OrderStatus.PENDENTE.canTransitionTo(OrderStatus.EM_PREPARO)).isFalse();
        }

        @Test
        @DisplayName("ENVIADO pode ir para EM_PREPARO")
        void enviado_canGoTo_emPreparo() {
            assertThat(OrderStatus.ENVIADO.canTransitionTo(OrderStatus.EM_PREPARO)).isTrue();
        }

        @Test
        @DisplayName("ENVIADO pode ir para AGUARDANDO_PAGAMENTO")
        void enviado_canGoTo_aguardandoPagamento() {
            assertThat(OrderStatus.ENVIADO.canTransitionTo(OrderStatus.AGUARDANDO_PAGAMENTO)).isTrue();
        }

        @Test
        @DisplayName("EM_PREPARO pode ir para PRONTO")
        void emPreparo_canGoTo_pronto() {
            assertThat(OrderStatus.EM_PREPARO.canTransitionTo(OrderStatus.PRONTO)).isTrue();
        }

        @Test
        @DisplayName("PRONTO pode ir para ENTREGUE")
        void pronto_canGoTo_entregue() {
            assertThat(OrderStatus.PRONTO.canTransitionTo(OrderStatus.ENTREGUE)).isTrue();
        }

        @Test
        @DisplayName("PRONTO pode ir para A_CAMINHO")
        void pronto_canGoTo_aCaminho() {
            assertThat(OrderStatus.PRONTO.canTransitionTo(OrderStatus.A_CAMINHO)).isTrue();
        }

        @Test
        @DisplayName("ENTREGUE não pode transitar para nenhum status")
        void entregue_cannotTransition() {
            for (OrderStatus s : OrderStatus.values()) {
                assertThat(OrderStatus.ENTREGUE.canTransitionTo(s)).isFalse();
            }
        }

        @Test
        @DisplayName("CANCELADO não pode transitar para nenhum status")
        void cancelado_cannotTransition() {
            for (OrderStatus s : OrderStatus.values()) {
                assertThat(OrderStatus.CANCELADO.canTransitionTo(s)).isFalse();
            }
        }

        @Test
        @DisplayName("A_CAMINHO pode ir para AGUARDANDO_PAGAMENTO")
        void aCaminho_canGoTo_aguardandoPagamento() {
            assertThat(OrderStatus.A_CAMINHO.canTransitionTo(OrderStatus.AGUARDANDO_PAGAMENTO)).isTrue();
        }

        @Test
        @DisplayName("A_CAMINHO pode ir para ENTREGUE")
        void aCaminho_canGoTo_entregue() {
            assertThat(OrderStatus.A_CAMINHO.canTransitionTo(OrderStatus.ENTREGUE)).isTrue();
        }

        @Test
        @DisplayName("AGUARDANDO_PAGAMENTO pode ir para ENTREGUE")
        void aguardandoPagamento_canGoTo_entregue() {
            assertThat(OrderStatus.AGUARDANDO_PAGAMENTO.canTransitionTo(OrderStatus.ENTREGUE)).isTrue();
        }

        @Test
        @DisplayName("AGUARDANDO_PAGAMENTO pode ir para CANCELADO")
        void aguardandoPagamento_canGoTo_cancelado() {
            assertThat(OrderStatus.AGUARDANDO_PAGAMENTO.canTransitionTo(OrderStatus.CANCELADO)).isTrue();
        }
    }

    @Nested
    @DisplayName("getDescription()")
    class GetDescription {

        @Test
        @DisplayName("retorna descrição não nula para todos os status")
        void getDescription_notNull_forAllStatuses() {
            for (OrderStatus s : OrderStatus.values()) {
                assertThat(s.getDescription()).isNotNull().isNotBlank();
            }
        }
    }
}
