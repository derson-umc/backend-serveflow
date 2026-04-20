package com.serveflow.Model.Order;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final Cep cep;
    private final Street street;
    private final City city;
    private final State state;
    private final Number number;
    private final Complement complement;

    private Address(UUID id, Cep cep, Street street, City city,
                    State state, Number number, Complement complement) {

        if (street == null) throw new IllegalArgumentException("Rua é obrigatória.");
        if (city == null) throw new IllegalArgumentException("Cidade é obrigatória.");
        if (state == null) throw new IllegalArgumentException("Estado é obrigatório.");
        if (number == null) throw new IllegalArgumentException("Número é obrigatório.");

        this.id = id;
        this.cep = cep;
        this.street = street;
        this.city = city;
        this.state = state;
        this.number = number;
        this.complement = complement;
    }

    public static Address create(Cep cep, Street street, City city,
                                 State state, Number number, Complement complement) {
        return new Address(null, cep, street, city, state, number, complement);
    }

    public static Address withId(UUID id, Cep cep, Street street, City city,
                                 State state, Number number, Complement complement) {
        return new Address(id, cep, street, city, state, number, complement);
    }
}
