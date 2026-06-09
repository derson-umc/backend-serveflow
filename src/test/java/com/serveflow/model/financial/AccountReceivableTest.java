package com.serveflow.model.financial;

import com.serveflow.exception.financial.DuplicateSettlementException;
import com.serveflow.exception.financial.InconsistentAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountReceivableTest {

    private static AccountReceivable pending() {
        return AccountReceivable.create("Venda #001", LocalDate.now().plusDays(5),
                new BigDecimal("300.00"), "Vendas", UUID.randomUUID());
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("cria conta a receber com status PENDING e campos corretos")
        void create_success() {
            UUID orderId = UUID.randomUUID();
            AccountReceivable ar = AccountReceivable.create("Venda #002",
                    LocalDate.now().plusDays(3), new BigDecimal("150.00"), "Vendas", orderId);

            assertThat(ar.getId()).isNotNull();
            assertThat(ar.getStatus()).isEqualTo(AccountStatus.PENDING);
            assertThat(ar.getDescription()).isEqualTo("Venda #002");
            assertThat(ar.getAmount()).isEqualByComparingTo("150.00");
            assertThat(ar.getCategory()).isEqualTo("Vendas");
            assertThat(ar.getSourceOrderId()).isEqualTo(orderId);
            assertThat(ar.getReceivedAt()).isNull();
            assertThat(ar.getReceivedAmount()).isNull();
            assertThat(ar.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando valor nulo")
        void create_throwsWhenAmountNull() {
            assertThatThrownBy(() -> AccountReceivable.create("X", LocalDate.now(), null, "Cat", null))
                    .isInstanceOf(InconsistentAmountException.class);
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando valor <= 0")
        void create_throwsWhenAmountNegative() {
            assertThatThrownBy(() -> AccountReceivable.create("X", LocalDate.now(),
                    new BigDecimal("-1.00"), "Cat", null))
                    .isInstanceOf(InconsistentAmountException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("registerReceipt()")
    class RegisterReceipt {

        @Test
        @DisplayName("registra recebimento em conta PENDING e muda status para RECEIVED")
        void registerReceipt_success() {
            AccountReceivable ar = pending();
            ar.registerReceipt(new BigDecimal("300.00"));
            assertThat(ar.getStatus()).isEqualTo(AccountStatus.RECEIVED);
            assertThat(ar.getReceivedAmount()).isEqualByComparingTo("300.00");
            assertThat(ar.getReceivedAt()).isNotNull();
        }

        @Test
        @DisplayName("lança DuplicateSettlementException quando conta já RECEIVED")
        void registerReceipt_throwsWhenAlreadyReceived() {
            AccountReceivable ar = pending();
            ar.registerReceipt(new BigDecimal("300.00"));
            assertThatThrownBy(() -> ar.registerReceipt(new BigDecimal("300.00")))
                    .isInstanceOf(DuplicateSettlementException.class);
        }

        @Test
        @DisplayName("lança DuplicateSettlementException quando conta CANCELLED")
        void registerReceipt_throwsWhenCancelled() {
            AccountReceivable ar = pending();
            ar.cancel();
            assertThatThrownBy(() -> ar.registerReceipt(new BigDecimal("300.00")))
                    .isInstanceOf(DuplicateSettlementException.class);
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando valor nulo")
        void registerReceipt_throwsWhenAmountNull() {
            AccountReceivable ar = pending();
            assertThatThrownBy(() -> ar.registerReceipt(null))
                    .isInstanceOf(InconsistentAmountException.class);
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando valor <= 0")
        void registerReceipt_throwsWhenAmountZero() {
            AccountReceivable ar = pending();
            assertThatThrownBy(() -> ar.registerReceipt(BigDecimal.ZERO))
                    .isInstanceOf(InconsistentAmountException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("cancela conta PENDING e muda status para CANCELLED")
        void cancel_pending_success() {
            AccountReceivable ar = pending();
            ar.cancel();
            assertThat(ar.getStatus()).isEqualTo(AccountStatus.CANCELLED);
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando conta já RECEIVED")
        void cancel_throwsWhenReceived() {
            AccountReceivable ar = pending();
            ar.registerReceipt(new BigDecimal("300.00"));
            assertThatThrownBy(ar::cancel)
                    .isInstanceOf(InconsistentAmountException.class)
                    .hasMessageContaining("received account");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("markOverdue()")
    class MarkOverdue {

        @Test
        @DisplayName("muda status para OVERDUE quando PENDING e data vencida")
        void markOverdue_pending_pastDue() {
            AccountReceivable ar = AccountReceivable.create("X", LocalDate.now().minusDays(1),
                    new BigDecimal("100.00"), "Cat", null);
            ar.markOverdue();
            assertThat(ar.getStatus()).isEqualTo(AccountStatus.OVERDUE);
        }

        @Test
        @DisplayName("não altera status quando PENDING e data não vencida")
        void markOverdue_pending_notYetDue() {
            AccountReceivable ar = pending(); // dueDate = now+5
            ar.markOverdue();
            assertThat(ar.getStatus()).isEqualTo(AccountStatus.PENDING);
        }

        @Test
        @DisplayName("não altera status quando conta RECEIVED")
        void markOverdue_received_noChange() {
            AccountReceivable ar = AccountReceivable.create("X", LocalDate.now().minusDays(1),
                    new BigDecimal("100.00"), "Cat", null);
            ar.registerReceipt(new BigDecimal("100.00"));
            ar.markOverdue();
            assertThat(ar.getStatus()).isEqualTo(AccountStatus.RECEIVED);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor direto")
    class FullConstructor {

        @Test
        @DisplayName("cria AccountReceivable com todos os campos via construtor")
        void fullConstructor_allFields() {
            UUID id = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            AccountReceivable ar = new AccountReceivable(id, "Desc", LocalDate.now(),
                    new BigDecimal("200.00"), AccountStatus.RECEIVED, now,
                    new BigDecimal("200.00"), "Vendas", orderId, now, 2L);

            assertThat(ar.getId()).isEqualTo(id);
            assertThat(ar.getSourceOrderId()).isEqualTo(orderId);
            assertThat(ar.getVersion()).isEqualTo(2L);
            assertThat(ar.getStatus()).isEqualTo(AccountStatus.RECEIVED);
        }
    }
}
