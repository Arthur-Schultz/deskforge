package com.deskforge.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
        @NotEmpty(message = "O carrinho está vazio") @Size(max = 30) List<@Valid Item> items,
        @NotNull @Valid Address shippingAddress,
        @AssertTrue(message = "É necessário aceitar os Termos de Serviço e a Política de Reembolso") boolean acceptTerms) {

    public record Item(
            @NotNull Long productId,
            @Min(value = 1, message = "Quantidade mínima é 1") @Max(value = 10, message = "Quantidade máxima por item é 10") int quantity) {
    }

    public record Address(
            @NotBlank(message = "Informe o destinatário") @Size(max = 120) String recipientName,
            @NotBlank @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido") String zipCode,
            @NotBlank(message = "Informe a rua") @Size(max = 160) String street,
            @NotBlank(message = "Informe o número") @Size(max = 20) String number,
            @Size(max = 80) String complement,
            @NotBlank(message = "Informe o bairro") @Size(max = 80) String district,
            @NotBlank(message = "Informe a cidade") @Size(max = 80) String city,
            @NotBlank @Pattern(regexp = "^[A-Z]{2}$", message = "UF inválida") String state) {
    }
}
