package com.serveflow.model.address;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StateTest {

    @Test
    @DisplayName("cria State com UF válida em maiúsculas")
    void create_validUfUpperCase() {
        State state = new State("SP");
        assertThat(state.getValue()).isEqualTo(State.Uf.SP);
    }

    @Test
    @DisplayName("cria State com UF válida em minúsculas (normaliza)")
    void create_validUfLowerCase() {
        State state = new State("sp");
        assertThat(state.getValue()).isEqualTo(State.Uf.SP);
    }

    @Test
    @DisplayName("cria State com espaços (faz strip)")
    void create_withSpaces() {
        State state = new State("  RJ  ");
        assertThat(state.getValue()).isEqualTo(State.Uf.RJ);
    }

    @Test
    @DisplayName("lança IllegalArgumentException para UF inválida")
    void create_invalidUf_throws() {
        assertThatThrownBy(() -> new State("ZZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado inválido");
    }

    @Test
    @DisplayName("todos os 27 valores do enum Uf são válidos")
    void allEnumValues_areValid() {
        for (State.Uf uf : State.Uf.values()) {
            State state = new State(uf.name());
            assertThat(state.getValue()).isEqualTo(uf);
        }
    }

    @Test
    @DisplayName("todos os estados brasileiros estão presentes no enum")
    void enumContains27States() {
        assertThat(State.Uf.values()).hasSize(27);
    }

    @Test
    @DisplayName("equals e hashCode baseados no valor")
    void equalsAndHashCode() {
        State s1 = new State("SP");
        State s2 = new State("SP");
        assertThat(s1).isEqualTo(s2);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    }

    @Test
    @DisplayName("estados diferentes não são iguais")
    void notEquals_differentStates() {
        State s1 = new State("SP");
        State s2 = new State("RJ");
        assertThat(s1).isNotEqualTo(s2);
    }

    @Test
    @DisplayName("verifica alguns estados específicos")
    void specificStates() {
        assertThat(new State("AC").getValue()).isEqualTo(State.Uf.AC);
        assertThat(new State("AM").getValue()).isEqualTo(State.Uf.AM);
        assertThat(new State("BA").getValue()).isEqualTo(State.Uf.BA);
        assertThat(new State("DF").getValue()).isEqualTo(State.Uf.DF);
        assertThat(new State("MG").getValue()).isEqualTo(State.Uf.MG);
        assertThat(new State("TO").getValue()).isEqualTo(State.Uf.TO);
    }
}
