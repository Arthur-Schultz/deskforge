package com.deskforge.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

import com.deskforge.catalog.ArtKind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 40) String sku,
        @NotBlank @Size(max = 120) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Use letras minúsculas, números e hífens") String slug,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 255) String shortDescription,
        @NotBlank @Size(max = 4000) String description,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal compareAtPrice,
        @Min(0) int stock,
        @NotNull ArtKind art,
        @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Use uma cor hexadecimal, ex.: #FF6B2C") String accentColor,
        boolean featured,
        @NotBlank String categorySlug,
        @Valid @Size(max = 12) List<SpecRequest> specs) {

    public record SpecRequest(@NotBlank @Size(max = 60) String label, @NotBlank @Size(max = 160) String value) {
    }
}
