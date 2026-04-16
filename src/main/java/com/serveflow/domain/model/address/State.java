package com.serveflow.domain.model.address;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class State {

    private final Uf value;

    public State(String value) {
        try {
            this.value = Uf.valueOf(value.toUpperCase().strip());
        } catch (Exception e) {
            throw new IllegalArgumentException("Estado inválido.");
        }
    }

    public enum Uf {
        AC, AL, AP, AM, BA, CE, DF, ES, GO,
        MA, MT, MS, MG, PA, PB, PR, PE, PI,
        RJ, RN, RS, RO, RR, SC, SP, SE, TO
    }
}
