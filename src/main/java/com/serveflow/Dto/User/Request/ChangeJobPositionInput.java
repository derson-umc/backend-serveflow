package com.serveflow.Dto.User.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para alteração somente do cargo (jobposition) de um usuário.
 * Operação restrita a perfis administrativos.
 */
public record ChangeJobPositionInput(

        @NotBlank(message = "Cargo é obrigatório")
        @Size(min = 2, max = 60, message = "Cargo deve ter entre 2 e 60 caracteres")
        String jobposition

) {}
