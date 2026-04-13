package com.serveflow.infrastructure.integration;

import com.serveflow.domain.model.address.Address;
import com.serveflow.domain.repository.AddressRepository;
import com.serveflow.web.dto.address.AddressResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class FindPostalCode implements AddressRepository {

    private static final Logger log = LoggerFactory.getLogger(FindPostalCode.class);
    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestClient restClient;

    public FindPostalCode(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public Optional<Address> findByCep(String cep) {
        String cleanCep = sanitize(cep);

        if (cleanCep.length() != 8) {
            return Optional.empty();
        }

        return executeLookup(cleanCep);
    }

    private String sanitize(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
    }

    private Optional<Address> executeLookup(String cep) {
        try {
            AddressResponseDTO response = restClient.get()
                    .uri(VIACEP_URL, cep)
                    .retrieve()
                    .body(AddressResponseDTO.class);

            if (response == null || response.erro()) {
                return Optional.empty();
            }

            return Optional.of(new Address(
                    response.cep(), response.logradouro(), response.localidade(),
                    response.uf(), "", response.complemento()
            ));

        } catch (RestClientException e) {
            log.warn("Falha na integração com ViaCEP para o CEP [{}]: {}", cep, e.getMessage());
            return Optional.empty();
        }
    }
}