package com.serveflow.web.dto.address.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressOutPut(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        boolean erro
) {}
