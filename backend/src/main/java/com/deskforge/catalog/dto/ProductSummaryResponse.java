package com.deskforge.catalog.dto;

import java.math.BigDecimal;

import com.deskforge.catalog.ArtKind;
import com.deskforge.catalog.Product;

public record ProductSummaryResponse(Long id, String sku, String slug, String name, String shortDescription,
                                     BigDecimal price, BigDecimal compareAtPrice, int stock, ArtKind art,
                                     String accentColor, boolean featured, boolean active, String categorySlug,
                                     String categoryName) {

    public static ProductSummaryResponse from(Product p) {
        return new ProductSummaryResponse(p.getId(), p.getSku(), p.getSlug(), p.getName(), p.getShortDescription(),
                p.getPrice(), p.getCompareAtPrice(), p.getStock(), p.getArt(), p.getAccentColor(), p.isFeatured(), p.isActive(),
                p.getCategory().getSlug(), p.getCategory().getName());
    }
}
