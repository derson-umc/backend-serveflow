package com.serveflow.Dto.PasswordReset.Request;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(

        @NotBlank(message = "Username é obrigatório")
        String username
) {}
