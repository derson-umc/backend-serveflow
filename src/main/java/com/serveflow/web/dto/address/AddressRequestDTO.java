package com.serveflow.web.dto.address;

import jakarta.validation.constraints.Size;

public record AddressRequestDTO(

        @Size(max = 10, message = "CEP deve ter no maximo 10 caracteres")
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
