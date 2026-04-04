package com.serveflow.infrastructure.integration;

import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.repository.AddressRepository;
import com.serveflow.web.dto.address.AddressResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Optional;

@Component
public class PostalCodeLookupGateway implements AddressRepository {

    private static final Logger log = LoggerFactory.getLogger(PostalCodeLookupGateway.class);
    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestClient restClient;

    public PostalCodeLookupGateway(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public Optional<Address> findByCep(String cep) {
        return sanitize(cep).flatMap(this::lookup);
    }

    private Optional<String> sanitize(String cep) {
        if (cep == null) return Optional.empty();
        String digits = cep.replaceAll("\\D", "");
        return digits.length() == 8 ? Optional.of(digits) : Optional.empty();
    }

    private Optional<Address> lookup(String cep) {
        try {
            return Optional.ofNullable(restClient.get()
                            .uri(VIACEP_URL, cep)
                            .retrieve()
                            .body(AddressResponseDTO.class))
                    .filter(r -> !r.erro())
                    .map(r -> new Address(r.cep(), r.logradouro(), r.localidade(), r.uf(), "", r.complemento()));
        } catch (Exception e) {
            log.warn("Falha ao consultar ViaCEP para CEP {}: {}", cep, e.getMessage());
            return Optional.empty();
        }
    }
}