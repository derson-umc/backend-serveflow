package com.serveflow.Model.Address;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Cep {

    private static final String REGEX = "\\d{5}-?\\d{3}";

    private final String value;

    public Cep(String value) {
        if (value == null || !value.matches(REGEX)) {
            throw new IllegalArgumentException("CEP inválido.");
        }
        this.value = value.replace("-", "");
    }
}
