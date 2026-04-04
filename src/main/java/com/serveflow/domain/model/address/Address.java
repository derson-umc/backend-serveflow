package com.serveflow.domain.model.address;

import java.util.Objects;
import java.util.UUID;

public class Address {

    private UUID id;
    private final String cep;
    private final String street;
    private final String city;
    private final String state;
    private final String number;
    private final String complement;

    public Address(String cep, String street, String city, String state,
                   String number, String complement) {
        if (street == null || street.isBlank())
            throw new IllegalArgumentException("Rua e obrigatoria.");
        if (city == null || city.isBlank())
            throw new IllegalArgumentException("Cidade e obrigatoria.");
        if (state == null || state.isBlank())
            throw new IllegalArgumentException("Estado e obrigatorio.");
        if (number == null)
            throw new IllegalArgumentException("Numero e obrigatorio.");

        this.cep = cep != null ? cep.strip() : null;
        this.street = street.strip();
        this.city = city.strip();
        this.state = state.strip();
        this.number = number.strip();
        this.complement = complement != null ? complement.strip() : null;
    }

    public Address(UUID id, String cep, String street, String city, String state,
                   String number, String complement) {
        this(cep, street, city, state, number, complement);
        this.id = id;
    }

    public UUID getId() { return id; }
    public String getCep() { return cep; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
