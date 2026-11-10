package com.deskforge.catalog.dto;

import jakarta.validation.constraints.Min;

public record StockUpdateRequest(@Min(value = 0, message = "O estoque não pode ser negativo") int stock) {
}
