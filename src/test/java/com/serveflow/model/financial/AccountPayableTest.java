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

class AccountPayableTest {

    private static AccountPayable pending() {
        return AccountPayable.create("Fornecedor ABC", LocalDate.now().plusDays(10),
                new BigDecimal("500.00"), "Material", "Fornecedor X");
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("cria conta a pagar com status PENDING e campos corretos")
        void create_success() {
            AccountPayable ap = pending();
            assertThat(ap.getId()).isNotNull();
            assertThat(ap.getStatus()).isEqualTo(AccountStatus.PENDING);
            assertThat(ap.getDescription()).isEqualTo("Fornecedor ABC");
            assertThat(ap.getAmount()).isEqualByComparingTo("500.00");
            assertThat(ap.getCategory()).isEqualTo("Material");
            assertThat(ap.getSupplier()).isEqualTo("Fornecedor X");
            assertThat(ap.getPaidAt()).isNull();
            assertThat(ap.getPaidAmount()).isNull();
            assertThat(ap.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando valor nulo")
        void create_throwsWhenAmountNull() {
            assertThatThrownBy(() -> AccountPayable.create("X", LocalDate.now().plusDays(1),
                    null, "Cat", "Sup"))
                    .isInstanceOf(InconsistentAmountException.class);
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando valor <= 0")
        void create_throwsWhenAmountZero() {
            assertThatThrownBy(() -> AccountPayable.create("X", LocalDate.now().plusDays(1),
                    BigDecimal.ZERO, "Cat", "Sup"))
                    .isInstanceOf(InconsistentAmountException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("registerPayment()")
    class RegisterPayment {

        @Test
        @DisplayName("paga conta PENDING e muda status para PAID")
        void registerPayment_success() {
            AccountPayable ap = pending();
            ap.registerPayment(new BigDecimal("500.00"));
            assertThat(ap.getStatus()).isEqualTo(AccountStatus.PAID);
            assertThat(ap.getPaidAmount()).isEqualByComparingTo("500.00");
            assertThat(ap.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("lança DuplicateSettlementException quando conta já PAID")
        void registerPayment_throwsWhenAlreadyPaid() {
            AccountPayable ap = pending();
            ap.registerPayment(new BigDecimal("500.00"));
            assertThatThrownBy(() -> ap.registerPayment(new BigDecimal("500.00")))
                    .isInstanceOf(DuplicateSettlementException.class);
        }

        @Test
        @DisplayName("lança DuplicateSettlementException quando conta CANCELLED")
        void registerPayment_throwsWhenCancelled() {
            AccountPayable ap = pending();
            ap.cancel();
            assertThatThrownBy(() -> ap.registerPayment(new BigDecimal("500.00")))
                    .isInstanceOf(DuplicateSettlementException.class);
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando valor nulo")
        void registerPayment_throwsWhenAmountNull() {
            AccountPayable ap = pending();
            assertThatThrownBy(() -> ap.registerPayment(null))
                    .isInstanceOf(InconsistentAmountException.class);
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando valor <= 0")
        void registerPayment_throwsWhenAmountZero() {
            AccountPayable ap = pending();
            assertThatThrownBy(() -> ap.registerPayment(BigDecimal.ZERO))
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
            AccountPayable ap = pending();
            ap.cancel();
            assertThat(ap.getStatus()).isEqualTo(AccountStatus.CANCELLED);
        }

        @Test
        @DisplayName("lança InconsistentAmountException quando conta já PAID")
        void cancel_throwsWhenPaid() {
            AccountPayable ap = pending();
            ap.registerPayment(new BigDecimal("500.00"));
            assertThatThrownBy(ap::cancel)
                    .isInstanceOf(InconsistentAmountException.class)
                    .hasMessageContaining("paid account");
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("markOverdue()")
    class MarkOverdue {

        @Test
        @DisplayName("muda status para OVERDUE quando PENDING e data vencida")
        void markOverdue_pending_pastDue() {
            AccountPayable ap = AccountPayable.create("X", LocalDate.now().minusDays(1),
                    new BigDecimal("100.00"), "Cat", "Sup");
            ap.markOverdue();
            assertThat(ap.getStatus()).isEqualTo(AccountStatus.OVERDUE);
        }

        @Test
        @DisplayName("não altera status quando PENDING e data não vencida")
        void markOverdue_pending_notYetDue() {
            AccountPayable ap = pending(); // dueDate = now+10
            ap.markOverdue();
            assertThat(ap.getStatus()).isEqualTo(AccountStatus.PENDING);
        }

        @Test
        @DisplayName("não altera status quando conta PAID")
        void markOverdue_paid_noChange() {
            AccountPayable ap = AccountPayable.create("X", LocalDate.now().minusDays(1),
                    new BigDecimal("100.00"), "Cat", "Sup");
            ap.registerPayment(new BigDecimal("100.00"));
            ap.markOverdue();
            assertThat(ap.getStatus()).isEqualTo(AccountStatus.PAID);
        }
    }

    // ──────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("construtor direto")
    class FullConstructor {

        @Test
        @DisplayName("cria AccountPayable com todos os campos via construtor")
        void fullConstructor_allFields() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            AccountPayable ap = new AccountPayable(id, "Desc", LocalDate.now(),
                    new BigDecimal("100.00"), AccountStatus.PAID, now,
                    new BigDecimal("100.00"), "Cat", "Sup", now, 1L);

            assertThat(ap.getId()).isEqualTo(id);
            assertThat(ap.getStatus()).isEqualTo(AccountStatus.PAID);
            assertThat(ap.getVersion()).isEqualTo(1L);
        }
    }
}
