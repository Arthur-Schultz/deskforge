package com.deskforge.privacy.dto;

import com.deskforge.privacy.DataSubjectRequestType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DataSubjectRequestPayload(
        @NotBlank(message = "Informe seu nome") @Size(max = 120) String fullName,
        @NotBlank(message = "Informe seu e-mail") @Email(message = "E-mail inválido") @Size(max = 160) String email,
        @NotNull(message = "Escolha o tipo de solicitação") DataSubjectRequestType type,
        @Size(max = 1000, message = "Use no máximo 1000 caracteres") String details) {
}
