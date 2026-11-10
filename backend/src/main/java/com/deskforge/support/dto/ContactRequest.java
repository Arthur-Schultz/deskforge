package com.deskforge.support.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Informe seu nome") @Size(max = 120) String fullName,
        @NotBlank(message = "Informe seu e-mail") @Email(message = "E-mail inválido") @Size(max = 160) String email,
        @NotBlank(message = "Informe o assunto") @Size(max = 120) String subject,
        @NotBlank(message = "Escreva sua mensagem") @Size(max = 2000, message = "Use no máximo 2000 caracteres") String message,
        @Pattern(regexp = "^$|^DF-\\d+$", message = "Número de pedido inválido") String orderNumber) {
}
