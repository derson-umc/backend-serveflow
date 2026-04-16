package com.serveflow.infrastructure.integration;

import com.serveflow.web.dto.address.response.AddressOutPut;

import java.util.Optional;

public interface PostalCodeClient {
    Optional<AddressOutPut> fetch(String cleanCep);
}
