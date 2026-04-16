package com.serveflow.infrastructure.integration;

import com.serveflow.web.dto.address.response.AddressOutPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class PostalCodeAdapter implements PostalCodeClient {

    private static final Logger log = LoggerFactory.getLogger(PostalCodeAdapter.class);
    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestClient restClient;

    public PostalCodeAdapter(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public Optional<AddressOutPut> fetch(String cleanCep) {
        try {
            AddressOutPut response = restClient.get()
                    .uri(VIACEP_URL, cleanCep)
                    .retrieve()
                    .body(AddressOutPut.class);
            return Optional.ofNullable(response);
        } catch (RestClientException e) {
            log.warn("Falha na integração com ViaCEP para o CEP [{}]: {}", cleanCep, e.getMessage());
            return Optional.empty();
        }
    }
}
