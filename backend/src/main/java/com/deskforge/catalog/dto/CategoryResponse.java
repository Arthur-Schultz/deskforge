package com.deskforge.catalog.dto;

import com.deskforge.catalog.Category;

public record CategoryResponse(Long id, String slug, String name, String description) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getSlug(), category.getName(),
                category.getDescription());
    }
}
