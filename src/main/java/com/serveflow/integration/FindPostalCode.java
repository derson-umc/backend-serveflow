package com.serveflow.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.serveflow.model.address.*;
import com.serveflow.model.address.Number;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class FindPostalCode {

    private static final Logger log = LoggerFactory.getLogger(FindPostalCode.class);

    private final String viaCepUrl;
    private final RestClient restClient;

    public FindPostalCode(@Value("${integration.viacep.url}") String viaCepUrl, RestClient.Builder builder) {
        this.viaCepUrl = viaCepUrl;
        this.restClient = builder.build();
    }

    public Optional<Address> findByCep(String cep) {
        String cleanCep = sanitize(cep);
        if (cleanCep.length() != 8) {
            return Optional.empty();
        }
        return fetch(cleanCep).flatMap(this::map);
    }

    private Optional<PostalCodeResponse> fetch(String cleanCep) {
        try {
            PostalCodeResponse response = restClient.get()
                    .uri(viaCepUrl, cleanCep)
                    .retrieve()
                    .body(PostalCodeResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientException e) {
            log.warn("Falha na integração com ViaCEP para o CEP [{}]: {}", cleanCep, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Address> map(PostalCodeResponse response) {
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

    private String sanitize(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PostalCodeResponse(
            String cep,
            String logradouro,
            String complemento,
            String bairro,
            String localidade,
            String uf,
            boolean erro
    ) {}
}
