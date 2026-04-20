package com.serveflow.Model.Order;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Number {

    private final String value;

    public Number(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Número é obrigatório.");
        this.value = value.strip();
    }
}
