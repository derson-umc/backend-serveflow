package com.serveflow.domain.model.address;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Complement {

    private final String value;

    public Complement(String value) {
        this.value = value == null ? null : value.strip();
    }
}