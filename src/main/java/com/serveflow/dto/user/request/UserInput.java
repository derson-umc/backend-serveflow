package com.serveflow.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.serveflow.model.user.UserRole;

public record UserInput(

        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String password,

        @NotNull(message = "Perfil é obrigatório")
        UserRole role,

        @NotBlank(message = "Cargo é obrigatório")
        @Size(min = 2, max = 60, message = "Cargo deve ter entre 2 e 60 caracteres")
        String jobposition

) {}
