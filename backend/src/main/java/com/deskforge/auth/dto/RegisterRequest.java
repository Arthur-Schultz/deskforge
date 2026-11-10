package com.deskforge.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Informe seu nome")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
        String fullName,

        @NotBlank(message = "Informe seu e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 160)
        String email,

        @NotBlank(message = "Informe uma senha")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha deve conter letras e números")
        String password,

        @AssertTrue(message = "É necessário aceitar os Termos de Serviço e a Política de Privacidade")
        boolean acceptTerms,

        boolean marketingConsent) {
}
