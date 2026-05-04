package com.serveflow.Dto.User.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordInput(

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 8, message = "Nova senha deve ter no mínimo 8 caracteres")
        String newPassword

) {}
