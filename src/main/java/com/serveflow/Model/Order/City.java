package com.serveflow.Model.Order;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class City {

    private final String value;

    public City(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Cidade é obrigatória.");
        this.value = value.strip();
    }
}
