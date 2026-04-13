package com.serveflow.web.dto.address.request;

import jakarta.validation.constraints.Size;

public record AddressInput(

        @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
        String cep,

        @Size(max = 200)
        String street,

        @Size(max = 100)
        String city,

        @Size(max = 2)
        String state,

        @Size(max = 20)
        String number,

        @Size(max = 200)
        String complement
) {}
