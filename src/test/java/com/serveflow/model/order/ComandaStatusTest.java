package com.serveflow.model.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComandaStatusTest {

    @Nested
    @DisplayName("isClosed()")
    class IsClosed {

        @Test
        @DisplayName("FECHADA retorna true para isClosed()")
        void fechada_isClosed() {
            assertThat(ComandaStatus.FECHADA.isClosed()).isTrue();
        }

        @Test
        @DisplayName("ABERTA retorna false para isClosed()")
        void aberta_isNotClosed() {
            assertThat(ComandaStatus.ABERTA.isClosed()).isFalse();
        }

        @Test
        @DisplayName("EM_FECHAMENTO retorna false para isClosed()")
        void emFechamento_isNotClosed() {
            assertThat(ComandaStatus.EM_FECHAMENTO.isClosed()).isFalse();
        }
    }

    @Nested
    @DisplayName("getDescription()")
    class GetDescription {

        @Test
        @DisplayName("retorna descrição para ABERTA")
        void aberta_hasDescription() {
            assertThat(ComandaStatus.ABERTA.getDescription()).isEqualTo("Comanda aberta.");
        }

        @Test
        @DisplayName("retorna descrição para EM_FECHAMENTO")
        void emFechamento_hasDescription() {
            assertThat(ComandaStatus.EM_FECHAMENTO.getDescription()).isEqualTo("Em fechamento.");
        }

        @Test
        @DisplayName("retorna descrição para FECHADA")
        void fechada_hasDescription() {
            assertThat(ComandaStatus.FECHADA.getDescription()).isEqualTo("Comanda fechada.");
        }
    }
}
