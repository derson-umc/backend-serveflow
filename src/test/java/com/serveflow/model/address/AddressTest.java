package com.serveflow.model.address;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Address domain model")
class AddressTest {

    private Street street() { return new Street("Rua das Flores"); }
    private City city() { return new City("São Paulo"); }
    private State state() { return new State("SP"); }
    private Number number() { return new Number("42"); }
    private Cep cep() { return new Cep("01310-100"); }
    private Complement complement() { return new Complement("Apto 3"); }

    @Nested
    @DisplayName("create() factory")
    class Create {

        @Test
        @DisplayName("cria Address com todos os campos obrigatórios")
        void create_withAllFields() {
            Address address = Address.create(cep(), street(), city(), state(), number(), complement());

            assertThat(address.getStreet().getValue()).isEqualTo("Rua das Flores");
            assertThat(address.getCity().getValue()).isEqualTo("São Paulo");
            assertThat(address.getState()).isNotNull();
            assertThat(address.getNumber().getValue()).isEqualTo("42");
            assertThat(address.getCep()).isNotNull();
            assertThat(address.getComplement()).isNotNull();
            assertThat(address.getId()).isNull(); // create() has null id
        }

        @Test
        @DisplayName("cria Address sem CEP e complemento (nullable)")
        void create_withNullOptionalFields() {
            Address address = Address.create(null, street(), city(), state(), number(), null);

            assertThat(address.getCep()).isNull();
            assertThat(address.getComplement()).isNull();
        }

        @Test
        @DisplayName("lança exceção quando street é null")
        void create_throwsWhenStreetNull() {
            assertThatThrownBy(() -> Address.create(null, null, city(), state(), number(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rua");
        }

        @Test
        @DisplayName("lança exceção quando city é null")
        void create_throwsWhenCityNull() {
            assertThatThrownBy(() -> Address.create(null, street(), null, state(), number(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cidade");
        }

        @Test
        @DisplayName("lança exceção quando state é null")
        void create_throwsWhenStateNull() {
            assertThatThrownBy(() -> Address.create(null, street(), city(), null, number(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Estado");
        }

        @Test
        @DisplayName("lança exceção quando number é null")
        void create_throwsWhenNumberNull() {
            assertThatThrownBy(() -> Address.create(null, street(), city(), state(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Número");
        }
    }

    @Nested
    @DisplayName("withId() factory")
    class WithId {

        @Test
        @DisplayName("cria Address com ID específico")
        void withId_setsId() {
            UUID id = UUID.randomUUID();
            Address address = Address.withId(id, cep(), street(), city(), state(), number(), complement());

            assertThat(address.getId()).isEqualTo(id);
            assertThat(address.getStreet().getValue()).isEqualTo("Rua das Flores");
        }

        @Test
        @DisplayName("cria Address com ID null")
        void withId_withNullId() {
            Address address = Address.withId(null, cep(), street(), city(), state(), number(), null);

            assertThat(address.getId()).isNull();
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("dois Address com mesmo ID são iguais")
        void sameId_areEqual() {
            UUID id = UUID.randomUUID();
            Address a1 = Address.withId(id, cep(), street(), city(), state(), number(), null);
            Address a2 = Address.withId(id, cep(), new Street("Rua B"), city(), state(), number(), null);

            assertThat(a1).isEqualTo(a2);
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }

        @Test
        @DisplayName("dois Address com null ID são iguais entre si")
        void nullId_areEqual() {
            Address a1 = Address.create(null, street(), city(), state(), number(), null);
            Address a2 = Address.create(null, street(), city(), state(), number(), null);

            assertThat(a1).isEqualTo(a2);
        }
    }
}
