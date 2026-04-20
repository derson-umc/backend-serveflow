package com.serveflow.Model.Address;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Street {

    private final String value;

    public Street(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Rua é obrigatória.");
        this.value = value.strip();
    }
}
