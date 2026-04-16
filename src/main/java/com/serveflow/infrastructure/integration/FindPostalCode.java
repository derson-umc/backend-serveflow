package com.serveflow.infrastructure.integration;

import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.repository.AddressRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindPostalCode implements AddressRepository {

    private final PostalCodeClient client;
    private final PostalCodeMapper mapper;

    public FindPostalCode(PostalCodeClient client, PostalCodeMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public Optional<Address> findByCep(String cep) {
        String cleanCep = sanitize(cep);
        if (cleanCep.length() != 8) {
            return Optional.empty();
        }
        return client.fetch(cleanCep).flatMap(mapper::map);
    }

    private String sanitize(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
    }
}
