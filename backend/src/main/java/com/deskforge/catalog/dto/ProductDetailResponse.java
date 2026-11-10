package com.deskforge.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

import com.deskforge.catalog.ArtKind;
import com.deskforge.catalog.Product;

public record ProductDetailResponse(Long id, String sku, String slug, String name, String shortDescription,
                                    String description, BigDecimal price, BigDecimal compareAtPrice, int stock,
                                    ArtKind art, String accentColor, boolean featured, boolean active,
                                    CategoryResponse category, List<ProductSpecResponse> specs,
                                    List<ProductSummaryResponse> related) {

    public static ProductDetailResponse from(Product p, List<ProductSummaryResponse> related) {
        return new ProductDetailResponse(p.getId(), p.getSku(), p.getSlug(), p.getName(), p.getShortDescription(),
                p.getDescription(), p.getPrice(), p.getCompareAtPrice(), p.getStock(), p.getArt(),
                p.getAccentColor(), p.isFeatured(), p.isActive(), CategoryResponse.from(p.getCategory()),
                p.getSpecs().stream().map(s -> new ProductSpecResponse(s.getLabel(), s.getValue())).toList(),
                related);
    }
}
