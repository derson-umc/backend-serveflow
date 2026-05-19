package com.serveflow.model.address;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Complement {

    private final String value;

    public Complement(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Complemento é obrigatório.");
        this.value = value.strip();
    }
}
