package com.serveflow.model.cashier;

import com.serveflow.exception.cashier.CashSessionAlreadyClosedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CashSessionTest {

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("open()")
    class Open {

        @Test
        @DisplayName("cria sessão com status OPEN e campos corretos")
        void open_success() {
            CashSession session = CashSession.open(new BigDecimal("200.00"), "Abertura", "caixa1");

            assertThat(session.getId()).isNotNull();
            assertThat(session.getStatus()).isEqualTo(CashSessionStatus.OPEN);
            assertThat(session.getInitialBalance()).isEqualByComparingTo("200.00");
            assertThat(session.getObservation()).isEqualTo("Abertura");
            assertThat(session.getOpenedBy()).isEqualTo("caixa1");
            assertThat(session.getOpenedAt()).isNotNull();
            assertThat(session.getClosedAt()).isNull();
            assertThat(session.getClosedBy()).isNull();
            assertThat(session.getVersion()).isNull();
        }

        @Test
        @DisplayName("usa saldo zero quando initialBalance é nulo")
        void open_zeroBalance_whenNull() {
            CashSession session = CashSession.open(null, null, "op");
            assertThat(session.getInitialBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("gera id único a cada chamada")
        void open_generatesUniqueId() {
            CashSession s1 = CashSession.open(BigDecimal.TEN, null, "op");
            CashSession s2 = CashSession.open(BigDecimal.TEN, null, "op");
            assertThat(s1.getId()).isNotEqualTo(s2.getId());
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("close()")
    class Close {

        @Test
        @DisplayName("fecha sessão OPEN e preenche campos de fechamento")
        void close_success() {
            CashSession session = CashSession.open(BigDecimal.TEN, "obs", "caixa1");
            session.close("supervisor", "Fechamento do dia");

            assertThat(session.getStatus()).isEqualTo(CashSessionStatus.CLOSED);
            assertThat(session.getClosedBy()).isEqualTo("supervisor");
            assertThat(session.getClosingObservation()).isEqualTo("Fechamento do dia");
            assertThat(session.getClosedAt()).isNotNull();
            assertThat(session.getClosedAt()).isAfterOrEqualTo(session.getOpenedAt());
        }

        @Test
        @DisplayName("lança CashSessionAlreadyClosedException ao fechar sessão CLOSED")
        void close_throwsWhenAlreadyClosed() {
            CashSession session = CashSession.open(BigDecimal.TEN, null, "op");
            session.close("supervisor", null);

            assertThatThrownBy(() -> session.close("outro", "re-close"))
                    .isInstanceOf(CashSessionAlreadyClosedException.class)
                    .hasMessageContaining(session.getId().toString());
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor completo")
    class Constructor {

        @Test
        @DisplayName("cria sessão com todos os campos via construtor direto")
        void constructor_allFields() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            CashSession session = new CashSession(id, CashSessionStatus.CLOSED,
                    new BigDecimal("100.00"), "obs", now, now.plusHours(8),
                    "caixa1", "supervisor", "Fechamento", 1L);

            assertThat(session.getId()).isEqualTo(id);
            assertThat(session.getStatus()).isEqualTo(CashSessionStatus.CLOSED);
            assertThat(session.getInitialBalance()).isEqualByComparingTo("100.00");
            assertThat(session.getObservation()).isEqualTo("obs");
            assertThat(session.getOpenedAt()).isEqualTo(now);
            assertThat(session.getClosedAt()).isEqualTo(now.plusHours(8));
            assertThat(session.getOpenedBy()).isEqualTo("caixa1");
            assertThat(session.getClosedBy()).isEqualTo("supervisor");
            assertThat(session.getClosingObservation()).isEqualTo("Fechamento");
            assertThat(session.getVersion()).isEqualTo(1L);
        }
    }
}
