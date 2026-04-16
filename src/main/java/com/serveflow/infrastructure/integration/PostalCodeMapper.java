package com.serveflow.infrastructure.integration;

import com.serveflow.domain.model.address.*;
import com.serveflow.domain.model.address.Number;
import com.serveflow.web.dto.address.response.AddressOutPut;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostalCodeMapper {

    public Optional<Address> map(AddressOutPut response) {
        if (response == null || response.erro()) {
            return Optional.empty();
        }

        String logradouro = response.logradouro();
        if (logradouro == null || logradouro.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(Address.create(
                toCep(response.cep()),
                new Street(logradouro),
                new City(response.localidade()),
                new State(response.uf()),
                new Number("S/N"),
                toComplement(response.complemento())
        ));
    }

    private Cep toCep(String raw) {
        return raw != null && !raw.isBlank() ? new Cep(raw) : null;
    }

    private Complement toComplement(String raw) {
        return raw != null && !raw.isBlank() ? new Complement(raw) : null;
    }
}
