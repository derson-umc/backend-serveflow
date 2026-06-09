package com.serveflow.dto.order;

import com.serveflow.dto.order.request.AddressInput;
import com.serveflow.dto.order.request.EnsureValidDelivery;
import com.serveflow.dto.order.request.OrderInput;
import com.serveflow.dto.order.request.OrderItemInput;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EnsureValidDelivery validator")
class EnsureValidDeliveryTest {

    EnsureValidDelivery validator;

    @Mock ConstraintValidatorContext context;
    @Mock ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new EnsureValidDelivery();
        // Setup mock chain for constraint violations
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    private OrderItemInput item() {
        return new OrderItemInput(null, "Produto", 1, new BigDecimal("10.00"), null, "ALIMENTO", List.of());
    }

    private AddressInput address() {
        return new AddressInput(null, "Rua A", "São Paulo", "SP", "100", null);
    }

    @Nested
    @DisplayName("null / missing type — always valid")
    class NullCases {

        @Test
        @DisplayName("dto null retorna true")
        void nullDto_isValid() {
            assertThat(validator.isValid(null, context)).isTrue();
        }

        @Test
        @DisplayName("type null retorna true")
        void nullType_isValid() {
            OrderInput dto = new OrderInput("Cliente", null, null, null, null, null, List.of(item()));
            assertThat(validator.isValid(dto, context)).isTrue();
        }
    }

    @Nested
    @DisplayName("DELIVERY validations")
    class DeliveryValidations {

        @Test
        @DisplayName("DELIVERY sem endereço retorna false e adiciona violation")
        void delivery_withoutAddress_returnsFalse() {
            OrderInput dto = new OrderInput("Cliente", null, "DELIVERY", null, null, null, List.of(item()));

            boolean valid = validator.isValid(dto, context);

            assertThat(valid).isFalse();
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate(contains("Endereço"));
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("DELIVERY com endereço retorna true")
        void delivery_withAddress_returnsTrue() {
            OrderInput dto = new OrderInput("Cliente", address(), "DELIVERY", null, null, null, List.of(item()));
            assertThat(validator.isValid(dto, context)).isTrue();
        }

        @Test
        @DisplayName("delivery (lowercase) sem endereço retorna false")
        void deliveryLowercase_withoutAddress_returnsFalse() {
            OrderInput dto = new OrderInput("Cliente", null, "delivery", null, null, null, List.of(item()));
            assertThat(validator.isValid(dto, context)).isFalse();
        }
    }

    @Nested
    @DisplayName("MESA validations")
    class MesaValidations {

        @Test
        @DisplayName("MESA sem número da mesa retorna false e adiciona violation")
        void mesa_withoutTableNumber_returnsFalse() {
            OrderInput dto = new OrderInput("Cliente", null, "MESA", null, null, null, List.of(item()));

            boolean valid = validator.isValid(dto, context);

            assertThat(valid).isFalse();
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate(contains("mesa"));
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("MESA com número da mesa retorna true")
        void mesa_withTableNumber_returnsTrue() {
            OrderInput dto = new OrderInput("Cliente", null, "MESA", null, null, "5", List.of(item()));
            assertThat(validator.isValid(dto, context)).isTrue();
        }

        @Test
        @DisplayName("MESA com tableNumber em branco retorna false")
        void mesa_withBlankTableNumber_returnsFalse() {
            OrderInput dto = new OrderInput("Cliente", null, "MESA", null, null, "  ", List.of(item()));
            assertThat(validator.isValid(dto, context)).isFalse();
        }
    }

    @Nested
    @DisplayName("BALCAO validations")
    class BalcaoValidations {

        @Test
        @DisplayName("BALCAO não requer endereço nem mesa — retorna true")
        void balcao_returnsTrue() {
            OrderInput dto = new OrderInput("Cliente", null, "BALCAO", null, null, null, List.of(item()));
            assertThat(validator.isValid(dto, context)).isTrue();
        }
    }
}
