package com.deskforge.catalog;

import java.math.BigDecimal;

public record ProductFilter(String category, String search, BigDecimal minPrice, BigDecimal maxPrice,
                            boolean inStockOnly, boolean onSaleOnly) {
}
