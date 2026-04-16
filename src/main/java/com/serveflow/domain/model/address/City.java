package com.serveflow.domain.model.address;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class City {

    private final String value;

    public City(String value) {
        this.value = require(value, "Cidade é obrigatória.");
    }

    private String require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.strip();
    }
}
