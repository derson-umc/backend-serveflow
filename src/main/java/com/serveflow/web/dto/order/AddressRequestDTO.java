package com.serveflow.web.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequestDTO(

        @Size(max = 10, message = "CEP deve ter no maximo 10 caracteres")
        String cep,

        @NotBlank(message = "Rua e obrigatoria")
        @Size(max = 200)
        String street,

        @NotBlank(message = "Cidade e obrigatoria")
        @Size(max = 100)
        String city,

        @NotBlank(message = "Estado e obrigatorio")
        @Size(max = 2)
        String state,

        @NotBlank(message = "Numero e obrigatorio")
        @Size(max = 20)
        String number,

        @Size(max = 200)
        String complement
) {}
