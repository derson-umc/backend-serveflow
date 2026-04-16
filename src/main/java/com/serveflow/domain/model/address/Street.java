package com.serveflow.domain.model.address;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Street {

    private final String value;

    public Street(String value) {
        this.value = require(value, "Rua é obrigatória.");
    }

    private String require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.strip();
    }
}
