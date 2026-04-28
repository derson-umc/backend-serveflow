package com.serveflow.Dto.User.Request;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordInput(

        @NotBlank(message = "Username é obrigatório")
        String username

) {}
