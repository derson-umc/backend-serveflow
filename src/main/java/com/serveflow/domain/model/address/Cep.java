package com.serveflow.domain.model.address;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Cep {

    private final String value;

    private static final String REGEX = "\\d{5}-?\\d{3}";

    public Cep(String value) {
        if (value == null || !value.matches(REGEX)) {
            throw new IllegalArgumentException("CEP inválido.");
        }
        this.value = normalize(value);
    }

    private String normalize(String value) {
        return value.replace("-", "");
    }
}