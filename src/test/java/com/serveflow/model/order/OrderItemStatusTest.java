package com.serveflow.model.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemStatusTest {

    @Nested
    @DisplayName("isCanceled()")
    class IsCanceled {

        @Test
        @DisplayName("CANCELADO_ANTES_PREPARO retorna true")
        void canceladoAntesPreparo_isCanceled() {
            assertThat(OrderItemStatus.CANCELADO_ANTES_PREPARO.isCanceled()).isTrue();
        }

        @Test
        @DisplayName("CANCELADO_EM_PREPARO retorna true")
        void canceladoEmPreparo_isCanceled() {
            assertThat(OrderItemStatus.CANCELADO_EM_PREPARO.isCanceled()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = OrderItemStatus.class,
                names = {"CANCELADO_ANTES_PREPARO", "CANCELADO_EM_PREPARO"},
                mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("status não cancelados retornam false")
        void nonCanceled_statuses(OrderItemStatus status) {
            assertThat(status.isCanceled()).isFalse();
        }
    }
}
